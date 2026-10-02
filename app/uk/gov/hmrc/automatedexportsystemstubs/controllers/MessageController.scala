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

import play.api.mvc.*
import play.api.{Logger, Logging}
import uk.gov.hmrc.automatedexportsystemstubs.async.IE507.AckRule
import uk.gov.hmrc.automatedexportsystemstubs.async.IE906.IE906Engine
import uk.gov.hmrc.automatedexportsystemstubs.async.IE917.IE917Engine
import uk.gov.hmrc.automatedexportsystemstubs.controllers.actions.ValidatedRequestAction
import uk.gov.hmrc.automatedexportsystemstubs.models.{ActionCode, NotificationData}
import uk.gov.hmrc.automatedexportsystemstubs.services.NotificationService
import uk.gov.hmrc.automatedexportsystemstubs.sync.{SyncError, SyncErrorPolicy}
import uk.gov.hmrc.automatedexportsystemstubs.utils.{NotificationXmlBuilder, SyncErrorResponseHelper}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton()
class MessageController @Inject() (
  cc:                  ControllerComponents,
  notificationService: NotificationService,
  validatedAction:     ValidatedRequestAction
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging:

  override val logger = Logger(this.getClass)

  private def handleSync(
    syncError:     SyncError,
    correlationId: String
  )(using request: Request[?]): Future[Result] =
    logger.warn(
      s"Sync error - status: ${syncError.status}," +
        s"correlationId: $correlationId," +
        s"message: ${syncError.message}," +
        s"detail: ${syncError.detail}"
    )

    Future.successful(
      validatedAction
        .errorResponse(
          SyncErrorResponseHelper.createErrorResponse(
            status = syncError.status,
            correlationId = correlationId,
            errorMessage = syncError.message,
            detail = syncError.detail
          ),
          request
        )
    )

  private def handleAsync(
    xml:           NodeSeq,
    notification:  NotificationData,
    correlationId: String
  )(using request: Request[?], hc: HeaderCarrier): Future[Result] =
    def IE917Response(matches: Seq[IE917Engine.MatchResult]): Future[Result] =
      val xmlErrors: NodeSeq = matches.flatMap(IE917Engine.toXmlError)

      notificationService
        .sendIE917Notification(notification = notification, correlationId = correlationId, errors = xmlErrors)
        .map(_ => validatedAction.successResponse(request))
        .recover { case e =>
          logger.warn("Failed to send EI917 error notification", e)
          validatedAction.errorResponse(InternalServerError, request)
        }
    end IE917Response

    def IE906Response(matches: Seq[IE906Engine.MatchResult]): Future[Result] =
      val functionalErrors: NodeSeq = matches.flatMap(IE906Engine.toFunctionalError)

      notificationService
        .sendIE906Notification(notification = notification, correlationId = correlationId, errors = functionalErrors)
        .map(_ => validatedAction.successResponse(request))
        .recover { case e =>
          logger.warn("Failed to send IE906 error notification", e)
          validatedAction.errorResponse(InternalServerError, request)
        }
    end IE906Response

    def ackAcceptedResponse(): Future[Result] =
      notificationService
        .sendAckNotification(notification, ActionCode.Accepted, correlationId)
        .map(_ => validatedAction.successResponse(request))
        .recover { case e =>
          logger.error("Failed to send ACK Accepted notification", e)
          validatedAction.errorResponse(InternalServerError, request)
        }
    end ackAcceptedResponse

    def diversionResponse(): Future[Result] =
      notificationService
        .sendAckNotification(notification, ActionCode.Diversion, correlationId)
        .flatMap(_ => nonDiversionResponse())
        .recover { case e =>
          logger.error("Failed to send ACK Diversion notification", e)
          validatedAction.errorResponse(InternalServerError, request)
        }
    end diversionResponse

    def nonDiversionResponse(): Future[Result] =
      val IE917Matches: Seq[IE917Engine.MatchResult] = IE917Engine.allMatches(xml)

      if IE917Matches.nonEmpty then IE917Response(IE917Matches)
      else
        val IE906Matches: Seq[IE906Engine.MatchResult] = IE906Engine.allMatches(xml)

        if IE906Matches.nonEmpty then IE906Response(IE906Matches)
        else ackAcceptedResponse()
    end nonDiversionResponse

    if AckRule.anyDiversionMatches(xml) then diversionResponse()
    else nonDiversionResponse()
  end handleAsync

  def message(): Action[AnyContent] =
    validatedAction.async { implicit request =>
      val correlationId = request.headers.get("x-correlation-id").getOrElse("")

      request.body.asXml match
        case None =>
          logger.warn(s"Error: Invalid XML: ${request.body.toString}")
          val response =
            SyncErrorResponseHelper.createErrorResponse(
              status = BAD_REQUEST,
              correlationId = correlationId,
              errorMessage = "Expected XML body",
              detail = s"Payload submitted ${request.body.toString}"
            )

          Future.successful(
            validatedAction
              .errorResponse(BadRequest(response.toString), request)
          )

        case Some(xml) =>
          SyncErrorPolicy.syncErrorFor(xml) match
            case Some(syncErr) =>
              handleSync(syncErr, correlationId)
            case None =>
              val notification: NotificationData =
                NotificationXmlBuilder.parseIncomingAckXml(correlationId, xml)

              handleAsync(xml, notification, correlationId)
    }
  end message
end MessageController
