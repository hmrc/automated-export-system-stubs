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

package uk.gov.hmrc.automatedexportsystemstubs.connectors
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.ws.*
import uk.gov.hmrc.automatedexportsystemstubs.models.AckNotification
import uk.gov.hmrc.automatedexportsystemstubs.utils.NotificationXmlBuilder
import uk.gov.hmrc.http.HttpReads.Implicits
import uk.gov.hmrc.http.HttpReads.Implicits.{readEitherOf, throwOnFailure}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

@Singleton
class NotificationConnector @Inject() (
  http:                                                                     HttpClientV2,
  @Named("automated-export-system-notifications.base-url") notificationUrl: String,
  @Named("automated-export-system-notifications.bearer-token") token:       String
)(implicit executionContext: ExecutionContext):

  implicit def httpResponse: HttpReads[HttpResponse] =
    throwOnFailure(readEitherOf[HttpResponse](using Implicits.readRaw))

  private val logger: Logger = Logger(this.getClass.getName)

  def sendNotification(
    notificationData: AckNotification,
    correlationId:    String
  )(implicit hc: HeaderCarrier): Future[HttpResponse] =
    send(notificationData, correlationId) { (data, now) =>
      NotificationXmlBuilder.buildAckResponseXml(data, now)
    }

  def send906Notification(
    notificationData: AckNotification,
    correlationId:    String,
    xmlError:         List[Elem]
  )(implicit hc: HeaderCarrier): Future[HttpResponse] =
    send(notificationData, correlationId) { (data, now) =>
      NotificationXmlBuilder.buildIe906ResponseXml(data, now, xmlError)
    }

  private def send(
    notificationData: AckNotification,
    correlationId:    String
  )(
    buildXml: (AckNotification, String) => Elem
  )(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val currentDateTime = DateTime.now().toString("EEE, dd MMM yyyy HH:mm:ss z")
    val xmlPayload      = NotificationXmlBuilder.xmlToString(buildXml(notificationData, currentDateTime))

    logger.info(
      s"Sending notification to $notificationUrl for recipient: ${notificationData.eori}, MRN: ${notificationData.mrn}, correlationId: $correlationId"
    )

    http
      .post(url"$notificationUrl")
      .setHeader(
        "Authorization"    -> token,
        "Content-Type"     -> "application/xml",
        "x-correlation-id" -> correlationId
      )
      .withBody(xmlPayload)
      .execute[HttpResponse]
      .map { response =>
        logger.info(s"Notification sent successfully with status: ${response.status}")
        response
      }
      .recoverWith { case e: Exception =>
        logger.warn(s"Error sending notification: ${e.getMessage}", e)
        Future.failed(e)
      }
  }
