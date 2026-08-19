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

import play.api.http.Status
import play.api.mvc.BodyParsers
import play.api.test.Helpers.*
import play.api.test.Helpers
import uk.gov.hmrc.automatedexportsystemstubs.controllers.actions.ValidatedRequestAction
import uk.gov.hmrc.automatedexportsystemstubs.helpers.{AllMocks, BaseSpec, TestData}
import org.mockito.Mockito.when

class MessageControllerSpec extends BaseSpec with AllMocks:

  private def elementText(xml: scala.xml.Elem, name: String): String =
    (xml \\ name).text

  trait Setup:
    val requiredHeaders = Map("some-header" -> "header-val", "another-header" -> "another")
    when(mockAppConfig.requiredHeaders).thenReturn(requiredHeaders)
    val validatedRequestAction = ValidatedRequestAction(mock[BodyParsers.Default], mockAppConfig)
    val controller             = new MessageController(Helpers.stubControllerComponents(), validatedRequestAction)

  "POST /" - {

    "return 204 when all required headers are provided" in new Setup:
      val requestWithHeaders = fakeRequest.withHeaders(requiredHeaders.toSeq: _*)
      val result             = controller.message()(requestWithHeaders)
      status(result) shouldBe Status.NO_CONTENT

    "return 400 when required headers are missing" in new Setup:
      val requestWithHeaders = fakeRequest.withHeaders(("some-header", "header-value"))
      val result             = controller.message()(requestWithHeaders)
      status(result) shouldBe Status.BAD_REQUEST

  }

  trait MrnSetup:
    val requiredHeaders = Map(
      "x-forwarded-host"  -> "*",
      "x-correlation-id"  -> "*",
      "x-conversation-id" -> "*",
      "date"              -> "*",
      "authorization"     -> "*",
      "content-type"      -> "application/xml",
      "accept"            -> "application/xml",
      "message-type"      -> "aesIE507Request"
    )
    when(mockAppConfig.requiredHeaders).thenReturn(requiredHeaders)
    val validatedRequestAction = ValidatedRequestAction(mock[BodyParsers.Default], mockAppConfig)
    val controller             = new MessageController(Helpers.stubControllerComponents(), validatedRequestAction)

  "MRN error responses" - {
    "return correct XML error response when MRN ends in A0" in new MrnSetup:
      val request = TestData.requestWithMrn("24AB1234567890A0A0")
      val result  = controller.message()(request)
      status(result) shouldBe Status.UNAUTHORIZED

      header("x-correlation-id", result) shouldBe Some("some-correlation-id")
      header("date", result)               should not be empty

      val xml = scala.xml.XML.loadString(contentAsString(result))

      elementText(xml, "errorCode")     shouldBe "401"
      elementText(xml, "errorMessage")  shouldBe "UNAUTHORIZED"
      elementText(xml, "source")        shouldBe "AES front end"
      elementText(xml, "correlationId") shouldBe "some-correlation-id"
      elementText(xml, "timestamp")       should not be empty

    "return correct XML error response when MRN ends in A1" in new MrnSetup:
      val request = TestData.requestWithMrn("24AB1234567890A0A1")
      val result  = controller.message()(request)
      status(result) shouldBe Status.NOT_FOUND

      header("x-correlation-id", result) shouldBe Some("some-correlation-id")
      header("date", result)               should not be empty

      val xml = scala.xml.XML.loadString(contentAsString(result))

      elementText(xml, "errorCode")     shouldBe "404"
      elementText(xml, "errorMessage")  shouldBe "NOT_FOUND"
      elementText(xml, "source")        shouldBe "AES front end"
      elementText(xml, "correlationId") shouldBe "some-correlation-id"
      elementText(xml, "timestamp")       should not be empty

    "return correct XML error response when MRN ends in A2" in new MrnSetup:
      val request = TestData.requestWithMrn("24AB1234567890A0A2")
      val result  = controller.message()(request)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR

      header("x-correlation-id", result) shouldBe Some("some-correlation-id")
      header("date", result)               should not be empty

      val xml = scala.xml.XML.loadString(contentAsString(result))

      elementText(xml, "errorCode")     shouldBe "500"
      elementText(xml, "errorMessage")  shouldBe "INTERNAL_SERVER_ERROR"
      elementText(xml, "source")        shouldBe "AES front end"
      elementText(xml, "correlationId") shouldBe "some-correlation-id"
      elementText(xml, "timestamp")       should not be empty

    "return correct XML error response when MRN ends in A3" in new MrnSetup:
      val request = TestData.requestWithMrn("24AB1234567890A0A3")
      val result  = controller.message()(request)
      status(result) shouldBe Status.BAD_REQUEST

      header("x-correlation-id", result) shouldBe Some("some-correlation-id")
      header("date", result)               should not be empty

      val xml = scala.xml.XML.loadString(contentAsString(result))

      elementText(xml, "errorCode")     shouldBe "400"
      elementText(xml, "errorMessage")  shouldBe "VALIDATION_ERROR"
      elementText(xml, "source")        shouldBe "AES front end"
      elementText(xml, "correlationId") shouldBe "some-correlation-id"
      elementText(xml, "timestamp")       should not be empty
  }
