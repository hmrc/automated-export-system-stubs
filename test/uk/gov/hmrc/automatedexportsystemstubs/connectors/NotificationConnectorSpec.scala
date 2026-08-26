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

import org.mockito.ArgumentMatchers.{any, eq as mEq}
import org.mockito.Mockito.{verify, when}
import play.api.http.Status
import uk.gov.hmrc.automatedexportsystemstubs.helpers.BaseSpec
import uk.gov.hmrc.automatedexportsystemstubs.models.AckNotification
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse, StringContextOps}

import java.net.URL
import scala.concurrent.Future

class NotificationConnectorSpec extends BaseSpec:
  trait Setup:
    val baseUrl       = "http://localhost:9001/notification"
    val token         = "test-bearer-token"
    val correlationId = "some-correlationId"

    val connector: NotificationConnector =
      new NotificationConnector(
        http = mockHttpClient,
        notificationUrl = baseUrl,
        token = token
      )

  "sendAckNotification" - {

    "successfully send notification with valid request" in new Setup {
      implicit val hc: HeaderCarrier =
        HeaderCarrier(
          authorization = Some(Authorization("auth-token")),
          otherHeaders = Seq("x-correlation-id" -> "correlationId")
        )
      when(mockHttpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      val notification = AckNotification("GB123456789000", "some-correlationId", "26GB123456789ABCDE1")
      when(mockHttpClient.post(any())(any())) thenReturn mockRequestBuilder
      when(mockRequestBuilder.withBody(any[String])(any(), any(), any()))
        .thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any[(String, String)]))
        .thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute(any(), any()))
        .thenReturn(Future.successful(HttpResponse(204, "")))

      val result = connector.sendNotification(notification, correlationId).futureValue
      result.status shouldBe NO_CONTENT

      verify(mockHttpClient)
        .post(mEq(url"http://localhost:9001/notification"))(any[HeaderCarrier])
    }

  }

  "send906Notification" - {

    "successfully send notification with valid request" in new Setup {
      implicit val hc: HeaderCarrier =
        HeaderCarrier(
          authorization = Some(Authorization("auth-token")),
          otherHeaders = Seq("x-correlation-id" -> "correlationId")
        )

      val notification = AckNotification("GB123456789000", "some-correlationId", "26GB123456789ABCDEB0")
      val xmlErrors: List[scala.xml.Elem] =
        List(<XMLError>
          <errorPointer>/Body/MRN</errorPointer>
          <errorCode>90</errorCode>
          <errorText>ERR02</errorText>
          <originalAttributeValue>26GB123456789ABCDEB0</originalAttributeValue>
        </XMLError>)

      when(mockHttpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.withBody(any[String])(any(), any(), any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any[(String, String)])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute(any(), any())).thenReturn(Future.successful(HttpResponse(204, "")))

      val result = connector.send906Notification(notification, correlationId, xmlErrors).futureValue
      result.status shouldBe NO_CONTENT

      verify(mockHttpClient).post(mEq(url"http://localhost:9001/notification"))(any[HeaderCarrier])

      val bodyCaptor = org.mockito.ArgumentCaptor.forClass(classOf[String])
      verify(mockRequestBuilder).withBody(bodyCaptor.capture())(any(), any(), any())

      val sentXml = bodyCaptor.getValue
      sentXml should include("<messageType>CD906C</messageType>")
      sentXml should include("<messageCode>CC507C</messageCode>")
      sentXml should include("<XMLError>")
    }

  }
  "handle HTTP errors from downstream service" in new Setup {
    implicit val hc: HeaderCarrier =
      HeaderCarrier(
        authorization = Some(Authorization("auth-token")),
        otherHeaders = Seq("x-correlation-id" -> "correlationId")
      )
    when(mockHttpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)

    val notification = AckNotification("GB123456789000", "some-correlationId", "26GB123456789ABCDE1")
    when(mockHttpClient.post(any())(any())) thenReturn mockRequestBuilder
    when(mockRequestBuilder.withBody(any[String])(any(), any(), any()))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.setHeader(any[(String, String)]))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.execute(any(), any()))
      .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, "Server Error")))

    val result = connector.sendNotification(notification, correlationId)

    result.futureValue.status shouldBe Status.INTERNAL_SERVER_ERROR
  }
