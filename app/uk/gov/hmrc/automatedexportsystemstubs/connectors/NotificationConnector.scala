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
import uk.gov.hmrc.automatedexportsystemstubs.utils.NotificationXmlBuilder
import uk.gov.hmrc.http.HttpReads.Implicits
import uk.gov.hmrc.http.HttpReads.Implicits.{readEitherOf, throwOnFailure}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationConnector @Inject() (
  http:                                                                                   HttpClientV2,
  @Named("automated-export-system-notifications") baseUrl:                                String,
  @Named("microservice.services.automated-export-system-notifications.auth-token") token: String
)(implicit executionContext: ExecutionContext) {

  implicit def httpResponse: HttpReads[HttpResponse] = throwOnFailure(readEitherOf[HttpResponse](using Implicits.readRaw))

  implicit val logger: Logger = Logger(this.getClass.getName)

  def sendNotification(requestBody: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    try {
      val correlationId = hc
        .headers(Seq("x-correlation-id"))
        .headOption
        .map(_._2)
        .getOrElse(throw new IllegalArgumentException("Missing required header: x-correlation-id"))
      val notificationData = NotificationXmlBuilder.parseIncomingAckXml(correlationId, requestBody)
      val currentDateTime  = DateTime.now().toString("EEE, dd MMM yyyy HH:mm:ss z")
      val responseXml      = NotificationXmlBuilder.buildAckResponseXml(
        notificationData,
        currentDateTime
      )
      val xmlPayload = NotificationXmlBuilder.xmlToString(responseXml)

      logger.info(
        s"Sending notification to $baseUrl for recipient: ${notificationData.eori}, MRN: ${notificationData.mrn}, correlationId: $correlationId"
      )

      http
        .post(url"$baseUrl")
        .withBody(xmlPayload)
        .setHeader("Authorization" -> token)
        .setHeader("Content-Type" -> "application/xml")
        .setHeader("x-correlation-id" -> correlationId)
        .execute
        .map { response =>
          logger.info(s"Notification sent successfully with status: ${response.status}")
          response
        }
        .recover { case e: Exception =>
          logger.error(s"Error sending notification: ${e.getMessage}", e)
          throw e
        }
    } catch {
      case e: Exception =>
        logger.error(s"Error processing notification: ${e.getMessage}", e)
        Future.failed(e)
    }
}
