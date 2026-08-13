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
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse, StringContextOps}

import java.net.URL
import scala.concurrent.Future

class NotificationConnectorSpec extends BaseSpec:
  trait Setup:
    val baseUrl = "http://localhost:9001/notification"
    val token   = "test-bearer-token"

    val connector: NotificationConnector =
      new NotificationConnector(
        http = mockHttpClient,
        baseUrl = baseUrl,
        token = token
      )

  "sendNotification" - {

    "successfully send notification with valid request" in new Setup {
      implicit val hc: HeaderCarrier =
        HeaderCarrier(
          authorization = Some(Authorization("auth-token")),
          otherHeaders = Seq("x-correlation-id" -> "correlationId")
        )
      when(mockHttpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      val validXmlPayload =
        """<?xml version="1.0" encoding="UTF-8"?>
            |<AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            |  <Header>
            |    <messageSender>GB123456789000</messageSender>
            |  </Header>
            |  <Body>
            |    <MRN>26GB123456789ABCDE1</MRN>
            |  </Body>
            |</AESDigitalNotification>""".stripMargin

      when(mockHttpClient.post(any())(any())) thenReturn mockRequestBuilder
      when(mockRequestBuilder.withBody(any[String])(any(), any(), any()))
        .thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any[(String, String)]))
        .thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute(any(), any()))
        .thenReturn(Future.successful(HttpResponse(204, "")))

      val result = connector.sendNotification(validXmlPayload).futureValue
      result.status shouldBe NO_CONTENT

      verify(mockHttpClient)
        .post(mEq(url"http://localhost:9001/notification"))(any[HeaderCarrier])
    }

  }

  "throw exception when x-correlation-id header is missing" in new Setup {
    val validXmlPayload =
      """<?xml version="1.0" encoding="UTF-8"?>
            |<AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            |  <Header>
            |    <messageSender>GB123456789000</messageSender>
            |  </Header>
            |  <Body>
            |    <MRN>26GB123456789ABCDE1</MRN>
            |  </Body>
            |</AESDigitalNotification>""".stripMargin

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val result = connector.sendNotification(validXmlPayload)

    result.failed.futureValue          shouldBe a[IllegalArgumentException]
    result.failed.futureValue.getMessage should include("Missing required header: x-correlation-id")
  }

  "throw exception when XML payload is invalid" in new Setup {
    implicit val hc: HeaderCarrier =
      HeaderCarrier(
        authorization = Some(Authorization("auth-token")),
        otherHeaders = Seq("x-correlation-id" -> "correlationId")
      )
    when(mockHttpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
    when(mockHttpClient.post(any())(any())) thenReturn mockRequestBuilder

    val invalidXmlPayload = "<invalid>xml"

    val result = connector.sendNotification(invalidXmlPayload)

    result.failed.futureValue          shouldBe a[RuntimeException]
    result.failed.futureValue.getMessage should include("Failed to parse XML")
  }

  "throw exception when MRN or EORI is missing from payload" in new Setup {
    implicit val hc: HeaderCarrier =
      HeaderCarrier(
        authorization = Some(Authorization("auth-token")),
        otherHeaders = Seq("x-correlation-id" -> "correlationId")
      )
    when(mockHttpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
    when(mockHttpClient.post(any())(any())) thenReturn mockRequestBuilder

    val incompleteXmlPayload =
      """<?xml version="1.0" encoding="UTF-8"?>
            |<AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            |  <Header>
            |  </Header>
            |  <Body>
            |  </Body>
            |</AESDigitalNotification>""".stripMargin

    val result = connector.sendNotification(incompleteXmlPayload)

    result.failed.futureValue          shouldBe a[RuntimeException]
    result.failed.futureValue.getMessage should include("Missing required fields")
  }

  "handle HTTP errors from downstream service" in new Setup {
    implicit val hc: HeaderCarrier =
      HeaderCarrier(
        authorization = Some(Authorization("auth-token")),
        otherHeaders = Seq("x-correlation-id" -> "correlationId")
      )
    when(mockHttpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
    val validXmlPayload =
      """<?xml version="1.0" encoding="UTF-8"?>
            |<AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            |  <Header>
            |    <messageSender>GB123456789000</messageSender>
            |  </Header>
            |  <Body>
            |    <MRN>26GB123456789ABCDE1</MRN>
            |  </Body>
            |</AESDigitalNotification>""".stripMargin

    when(mockHttpClient.post(any())(any())) thenReturn mockRequestBuilder
    when(mockRequestBuilder.withBody(any[String])(any(), any(), any()))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.setHeader(any[(String, String)]))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.execute(any(), any()))
      .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, "Server Error")))

    val result = connector.sendNotification(validXmlPayload)

    result.futureValue.status shouldBe Status.INTERNAL_SERVER_ERROR
  }
