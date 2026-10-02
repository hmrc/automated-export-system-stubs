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
import play.api.Logger
import play.api.libs.ws.*
import uk.gov.hmrc.automatedexportsystemstubs.models.{ActionCode, NotificationData}
import uk.gov.hmrc.automatedexportsystemstubs.utils.NotificationXmlBuilder
import uk.gov.hmrc.http.HttpReads.Implicits
import uk.gov.hmrc.http.HttpReads.Implicits.{readEitherOf, throwOnFailure}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import java.time.format.DateTimeFormatter
import java.time.{Clock, Instant, ZoneOffset}
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, NodeSeq}

@Singleton
class NotificationConnector @Inject() (
  http:                                                                     HttpClientV2,
  clock:                                                                    Clock,
  @Named("automated-export-system-notifications.base-url") notificationUrl: String,
  @Named("automated-export-system-notifications.bearer-token") token:       String
)(implicit executionContext: ExecutionContext):

  implicit def httpResponse: HttpReads[HttpResponse] =
    throwOnFailure(readEitherOf[HttpResponse](using Implicits.readRaw))

  private val logger: Logger = Logger(this.getClass.getName)

  def sendNotification(
    notificationData: NotificationData,
    actionCode:       ActionCode,
    correlationId:    String
  )(implicit hc: HeaderCarrier): Future[HttpResponse] =
    send(notificationData, correlationId) { (data, now) =>
      NotificationXmlBuilder.buildAckResponseXml(data, actionCode, now)
    }

  def send906Notification(
    notificationData: NotificationData,
    correlationId:    String,
    functionalErrors: NodeSeq
  )(implicit hc: HeaderCarrier): Future[HttpResponse] =
    send(notificationData, correlationId) { (data, now) =>
      NotificationXmlBuilder.buildIE906ResponseXml(data, now, functionalErrors)
    }

  def send917Notification(
    notificationData: NotificationData,
    correlationId:    String,
    xmlErrors:        NodeSeq
  )(implicit hc: HeaderCarrier): Future[HttpResponse] =
    send(notificationData, correlationId) { (data, now) =>
      NotificationXmlBuilder.buildIE917ResponseXml(data, now, xmlErrors)
    }

  private def send(
    notificationData: NotificationData,
    correlationId:    String
  )(
    buildXml: (NotificationData, String) => Elem
  )(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val httpDateTimeUtcFormatter: DateTimeFormatter =
      DateTimeFormatter.RFC_1123_DATE_TIME
        .withZone(ZoneOffset.UTC)

    val instantNow:      Instant = Instant.now(clock)
    val httpDateTimeNow: String  = httpDateTimeUtcFormatter.format(instantNow)

    val xmlPayload: Elem = buildXml(notificationData, httpDateTimeNow)

    logger.info(
      s"Sending notification to $notificationUrl for recipient: ${notificationData.eori}, " +
        s"MRN: ${notificationData.mrn}, correlationId: $correlationId"
    )

    http
      .post(url"$notificationUrl")
      .setHeader(
        "Authorization"    -> s"Bearer $token",
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
