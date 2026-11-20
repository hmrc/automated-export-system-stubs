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

import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.mvc.*
import uk.gov.hmrc.automatedexportsystemstubs.config.AppConfig
import uk.gov.hmrc.automatedexportsystemstubs.controllers.actions.request.ValidatedRequest
import uk.gov.hmrc.automatedexportsystemstubs.utils.{DateHelper, SyncErrorResponseHelper}

import java.time.format.DateTimeFormatter
import java.time.{Clock, Instant, ZoneOffset, ZonedDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ValidatedRequestAction @Inject() (
  override val parser: BodyParsers.Default,
  appConfig:           AppConfig,
  clock:               Clock
)(implicit val executionContext: ExecutionContext)
    extends ActionRefiner[Request, ValidatedRequest]
    with ActionBuilder[ValidatedRequest, AnyContent]:

  private def currentHttpDate: String =
    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))

  private def buildResponse(result: Result, incomingRequest: Request[?]): Result = {
    val headers = result.withHeaders(HeaderNames.DATE -> currentHttpDate, HeaderNames.CONTENT_TYPE -> MimeTypes.XML)
    incomingRequest.headers
      .get("x-correlation-id")
      .fold(headers)(id => headers.withHeaders("x-correlation-id" -> id))
  }

  private def isValidHttpDate(value: String): Boolean =
    Try(DateHelper.httpDateFormatter.parse(value.trim)).isSuccess

  override protected def refine[A](request: Request[A]): Future[Either[Result, ValidatedRequest[A]]] =
    Future.successful {
      val invalidHeaders = appConfig.requiredHeaders.filter { case (key, expectedValue) =>
        request.headers.get(key) match {
          case None =>
            true

          case Some(actual) if expectedValue == "*" =>
            actual.trim.isEmpty ||
            actual.trim == "*" ||
            (key.equalsIgnoreCase("date") && !isValidHttpDate(actual))

          case Some(actual) if expectedValue.endsWith("*") =>
            val expectedPrefix = expectedValue.dropRight(1)

            !actual.startsWith(expectedPrefix) ||
            actual.drop(expectedPrefix.length).trim.isEmpty

          case Some(actual) =>
            actual != expectedValue
        }
      }

      if (invalidHeaders.nonEmpty) {
        val msg = s"Missing or invalid mandatory headers: ${invalidHeaders.keys.mkString(", ")}"

        val errorResponse: Result = SyncErrorResponseHelper.createErrorResponse(
          Status.BAD_REQUEST,
          timestamp = Instant.now(clock),
          correlationId = request.headers.get("x-correlation-id").getOrElse(""),
          errorMessage = msg,
          detail = msg
        )

        Left(
          buildResponse(
            errorResponse,
            request
          )
        )
      } else {
        Right(ValidatedRequest(request))
      }
    }

  def errorResponse(result: Result, request: Request[?]): Result =
    buildResponse(result, request)

  def successResponse(request: Request[?]): Result =
    buildResponse(Results.NoContent, request)
