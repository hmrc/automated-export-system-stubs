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

package uk.gov.hmrc.automatedexportsystemstubs.services

import org.apache.pekko.actor.ActorSystem
import play.api.Logging
import uk.gov.hmrc.automatedexportsystemstubs.config.AppConfig
import uk.gov.hmrc.automatedexportsystemstubs.connectors.NotificationConnector
import uk.gov.hmrc.automatedexportsystemstubs.models.AckNotification
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

@Singleton
class NotificationService @Inject() (
  notificationConnector: NotificationConnector,
  appConfig:             AppConfig,
  actorSystem:           ActorSystem
)(implicit ec: ExecutionContext)
    extends Logging:

  def sendAckNotification(
    notification:  AckNotification,
    correlationId: String
  )(implicit hc: HeaderCarrier): Future[Unit] =
    scheduleNotification("ACK", correlationId) {
      notificationConnector.sendNotification(
        notification,
        correlationId
      )
    }

  def sendIE906Notification(
    notification:  AckNotification,
    correlationId: String,
    errors:        List[Elem]
  )(implicit hc: HeaderCarrier): Future[Unit] =
    scheduleNotification("IE906", correlationId) {
      notificationConnector.send906Notification(
        notification,
        correlationId,
        errors
      )
    }

  def sendIE917Notification(
    notification:  AckNotification,
    correlationId: String,
    errors:        List[Elem]
  )(implicit hc: HeaderCarrier): Future[Unit] =
    scheduleNotification("IE917", correlationId) {
      notificationConnector.send917Notification(
        notification,
        correlationId,
        errors
      )
    }

  private def scheduleNotification(
    notificationType: String,
    correlationId:    String
  )(
    send: => Future[HttpResponse]
  ): Future[Unit] = {

    actorSystem.scheduler.scheduleOnce(appConfig.notificationDelay) {
      send.failed.foreach { error =>
        logger.warn(
          s"Failed to send $notificationType notification correlationId=$correlationId",
          error
        )
      }
    }

    Future.successful(())
  }
