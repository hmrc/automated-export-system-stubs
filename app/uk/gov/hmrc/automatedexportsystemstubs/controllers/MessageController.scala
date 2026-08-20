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

package uk.gov.hmrc.automatedexportsystemstubs.controllers

import play.api.{Logger, Logging}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.automatedexportsystemstubs.controllers.actions.ValidatedRequestAction
import uk.gov.hmrc.automatedexportsystemstubs.services.NotificationService
import uk.gov.hmrc.automatedexportsystemstubs.utils.{ErrorResponseHelper, NotificationXmlBuilder}
import uk.gov.hmrc.http.HeaderCarrier
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class MessageController @Inject() (
  cc:                  ControllerComponents,
  notificationService: NotificationService,
  validatedAction:     ValidatedRequestAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with Logging:

  override val logger = Logger(this.getClass)

  def message(): Action[AnyContent] = validatedAction.async { implicit request =>
    val correlationId = request.headers.get("x-correlation-id").getOrElse("")
    implicit val hc: HeaderCarrier =
      HeaderCarrier(
        extraHeaders = Seq("x-correlation-id" -> correlationId)
      )
    request.body.asXml match
      case Some(xml) =>
        val mrn = (xml \\ "MRN").headOption.map(_.text.trim)
        mrn match
          case Some(value) if value.endsWith("000") =>
            Future.successful(
              validatedAction.errorResponse(
                ErrorResponseHelper.createErrorResponse(
                  status = 401,
                  correlationId,
                  errorMessage = "UNAUTHORIZED",
                  detail = "Invalid or missing token"
                ),
                request
              )
            )

          case Some(value) if value.endsWith("001") =>
            Future.successful(
              validatedAction.errorResponse(
                ErrorResponseHelper.createErrorResponse(
                  status = 404,
                  correlationId,
                  errorMessage = "NOT_FOUND",
                  detail = "EIS endpoint not found"
                ),
                request
              )
            )

          case Some(value) if value.endsWith("002") =>
            Future.successful(
              validatedAction.errorResponse(
                ErrorResponseHelper.createErrorResponse(
                  status = 500,
                  correlationId,
                  errorMessage = "INTERNAL_SERVER_ERROR",
                  detail = "Server error"
                ),
                request
              )
            )

          case Some(value) if value.endsWith("003") =>
            Future.successful(
              validatedAction.errorResponse(
                ErrorResponseHelper.createErrorResponse(
                  status = 400,
                  correlationId,
                  errorMessage = "VALIDATION_ERROR",
                  detail = "Validation error"
                ),
                request
              )
            )
          case _ =>
            request.body.asXml
              .flatMap(_.headOption.collect { case e: scala.xml.Elem => e }) match {

              case Some(elem) =>
                val notification = NotificationXmlBuilder.parseIncomingAckXml(correlationId, elem)
                notificationService
                  .sendNotification(notification, correlationId)
                  .map { response =>
                    logger.info(s"Notification sent successfully: ${response.status}")
                    validatedAction.successResponse(request)
                  }
                  .recover { case e =>
                    logger.error("Failed to send notification", e)
                    validatedAction.errorResponse(InternalServerError, request)
                  }
              case None =>
                Future.successful(validatedAction.errorResponse(BadRequest("Expected XML body"), request))
            }
      case None =>
        Future.successful(validatedAction.successResponse(request))
  }
