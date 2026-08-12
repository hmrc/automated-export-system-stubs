/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.automatedexportsystemstubs.controllers.actions

import play.api.http.HeaderNames
import play.api.mvc.Results.{BadRequest, NoContent}
import play.api.mvc.*
import uk.gov.hmrc.automatedexportsystemstubs.config.AppConfig

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class ValidatedRequest[A](request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class ValidatedRequestAction @Inject() (
  override val parser: BodyParsers.Default,
  appConfig:           AppConfig
)(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[Request, ValidatedRequest]
    with ActionBuilder[ValidatedRequest, AnyContent] {

  private def currentHttpDate: String =
    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now())

  private def buildResponse(result: Result, incomingRequest: Request[?]): Result = {
    val withDate = result.withHeaders(HeaderNames.DATE -> currentHttpDate)

    incomingRequest.headers.get("x-correlation-id") match {
      case Some(id) => withDate.withHeaders("x-correlation-id" -> id)
      case None     => withDate
    }
  }

  override protected def refine[A](request: Request[A]): Future[Either[Result, ValidatedRequest[A]]] = Future.successful {

    def isValidHttpDate(value: String): Boolean =
      scala.util
        .Try(
          java.time.ZonedDateTime.parse(
            value,
            java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
          )
        )
        .isSuccess

    val invalidHeaders = appConfig.requiredHeaders.filter { case (key, expectedValue) =>
      request.headers.get(key) match {
        case None =>
          true
        case Some(actualValue)
            if expectedValue == "*" && (actualValue.trim == "*" || actualValue.isEmpty
              || (key.equals("date") && !isValidHttpDate(actualValue))) =>
          true
        case Some(actualValue) if expectedValue != "*" && actualValue != expectedValue =>
          true
        case _ =>
          false
      }
    }

    if (invalidHeaders.nonEmpty) {
      val errorMessage = s"Missing or invalid mandatory headers: ${invalidHeaders.keys.mkString(", ")}"
      Left(buildResponse(BadRequest(errorMessage), request))
    } else {
      Right(ValidatedRequest(request))
    }
  }

  def errorResponse(
    result:  Result,
    request: Request[?]
  ): Result =
    buildResponse(result, request)

  def successResponse(request: Request[?]): Result =
    buildResponse(NoContent, request)
}
