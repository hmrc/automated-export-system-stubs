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

import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.{eq as eqTo, *}
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.{AnyContentAsXml, BodyParsers, Result}
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.automatedexportsystemstubs.controllers.actions.ValidatedRequestAction
import uk.gov.hmrc.automatedexportsystemstubs.helpers.{AllMocks, BaseSpec, TestData, XmlOps}
import uk.gov.hmrc.automatedexportsystemstubs.models.{ActionCode, NotificationData}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.xml.Elem

class MessageControllerSpec extends BaseSpec with AllMocks:

  private def elementText(xml: scala.xml.Elem, name: String): String =
    (xml \\ name).text

  trait Setup:
    val requiredHeaders: Map[String, String] =
      Map("some-header" -> "header-val", "another-header" -> "another")

    when(mockAppConfig.requiredHeaders).thenReturn(requiredHeaders)
    when(
      mockNotificationService.sendAckNotification(
        any[NotificationData],
        any[ActionCode],
        any[String]
      )(any[HeaderCarrier])
    ).thenReturn(Future.successful(()))

    private val cc             = stubControllerComponents()
    private val bodyParsers    = new BodyParsers.Default(cc.parsers)
    val validatedRequestAction = ValidatedRequestAction(bodyParsers, mockAppConfig)

    val eoriNumber:    String = "GB123456789000"
    val mrn:           String = "26GB123456789ABCDEX9"
    val correlationId: String = "correlationId"

    val controller: MessageController =
      new MessageController(
        Helpers.stubControllerComponents(),
        mockNotificationService,
        validatedRequestAction
      )

  "POST /" - {

    "should return a 204 response" - {

      "when the response is ACK Accepted" in new Setup {
        val notificationData: NotificationData =
          NotificationData(
            eori = eoriNumber,
            correlationId = correlationId,
            mrn = mrn
          )

        when(
          mockNotificationService.sendAckNotification(
            eqTo(notificationData),
            eqTo(ActionCode.Accepted),
            eqTo(correlationId)
          )(any[HeaderCarrier])
        )
          .thenReturn(Future.successful(()))

        val xmlBody: Elem =
          <AESDigitalNotification>
            <Header>
              <messageSender>{eoriNumber}</messageSender>
            </Header>
            <Body>
              <MRN>{mrn}</MRN>
            </Body>
          </AESDigitalNotification>

        val requestWithHeaders: FakeRequest[AnyContentAsXml] =
          fakeRequest
            .withHeaders(requiredHeaders.toSeq: _*)
            .withHeaders("x-correlation-id" -> correlationId)
            .withXmlBody(xmlBody)

        val result: Future[Result] = controller.message()(requestWithHeaders)

        status(result)         shouldBe Status.NO_CONTENT
        contentType(result)    shouldBe None
        contentAsBytes(result) shouldBe ByteString.empty
      }

      "when the response is ACK Diversion" - {

        "followed by an ACK Accepted" in new Setup {
          val notificationData: NotificationData =
            NotificationData(
              eori = eoriNumber,
              correlationId = correlationId,
              mrn = mrn
            )

          when(
            mockNotificationService.sendAckNotification(
              eqTo(notificationData),
              eqTo(ActionCode.Diversion),
              eqTo(correlationId)
            )(any[HeaderCarrier])
          )
            .thenReturn(Future.successful(()))

          when(
            mockNotificationService.sendAckNotification(
              eqTo(notificationData),
              eqTo(ActionCode.Accepted),
              eqTo(correlationId)
            )(any[HeaderCarrier])
          )
            .thenReturn(Future.successful(()))

          val xmlBody: Elem =
            <AESDigitalNotification>
              <Header>
                <messageSender>{eoriNumber}</messageSender>
              </Header>
              <Body>
                <MRN>{mrn}</MRN>
                <GoodsShipment>
                  <Consignment>
                    <parentUCRID>ACKDIVERSIONBB</parentUCRID>
                  </Consignment>
                </GoodsShipment>
              </Body>
            </AESDigitalNotification>

          val requestWithHeaders: FakeRequest[AnyContentAsXml] =
            fakeRequest
              .withHeaders(requiredHeaders.toSeq: _*)
              .withHeaders("x-correlation-id" -> correlationId)
              .withXmlBody(xmlBody)

          val result: Future[Result] = controller.message()(requestWithHeaders)

          status(result)         shouldBe Status.NO_CONTENT
          contentType(result)    shouldBe None
          contentAsBytes(result) shouldBe ByteString.empty
        }

        "followed by an IE917 error" in new Setup {
          val notificationData: NotificationData =
            NotificationData(
              eori = eoriNumber,
              correlationId = correlationId,
              mrn = mrn
            )

          when(
            mockNotificationService.sendAckNotification(
              eqTo(notificationData),
              eqTo(ActionCode.Diversion),
              eqTo(correlationId)
            )(any[HeaderCarrier])
          )
            .thenReturn(Future.successful(()))

          val IE917ErrorXml: Elem =
            <XmlError>
              <errorPointer>Body.CustomsOfficeOExitActual.referenceNumber</errorPointer>
              <errorCode>12</errorCode>
              <errorText>ERR02</errorText>
              <originalAttributeValue>XMLERROR000</originalAttributeValue>
            </XmlError>

          when(
            mockNotificationService.sendIE917Notification(
              eqTo(notificationData),
              eqTo(correlationId),
              argThat(xml => XmlOps.normalize(xml) == XmlOps.normalize(IE917ErrorXml))
            )(any[HeaderCarrier])
          )
            .thenReturn(Future.successful(()))

          val xmlBody: Elem =
            <AESDigitalNotification>
              <Header>
                <messageSender>{eoriNumber}</messageSender>
              </Header>
              <Body>
                <MRN>{mrn}</MRN>
                <CustomsOfficeOExitActual>
                  <referenceNumber>XMLERROR000</referenceNumber>
                </CustomsOfficeOExitActual>
                <GoodsShipment>
                  <Consignment>
                    <parentUCRID>ACKDIVERSIONBB</parentUCRID>
                  </Consignment>
                </GoodsShipment>
              </Body>
            </AESDigitalNotification>

          val requestWithHeaders: FakeRequest[AnyContentAsXml] =
            fakeRequest
              .withHeaders(requiredHeaders.toSeq: _*)
              .withHeaders("x-correlation-id" -> correlationId)
              .withXmlBody(xmlBody)

          val result: Future[Result] = controller.message()(requestWithHeaders)

          status(result)         shouldBe Status.NO_CONTENT
          contentType(result)    shouldBe None
          contentAsBytes(result) shouldBe ByteString.empty
        }

        "followed by an IE906 error" in new Setup {
          val notificationData: NotificationData =
            NotificationData(
              eori = eoriNumber,
              correlationId = correlationId,
              mrn = mrn
            )

          when(
            mockNotificationService.sendAckNotification(
              eqTo(notificationData),
              eqTo(ActionCode.Diversion),
              eqTo(correlationId)
            )(any[HeaderCarrier])
          )
            .thenReturn(Future.successful(()))

          val IE906ErrorXml: Elem =
            <FunctionalError>
              <errorPointer>Body.GoodsShipment.Consignment.ReferenceNumberUCRID</errorPointer>
              <errorCode>8</errorCode>
              <errorReason>ERR02</errorReason>
              <originalAttributeValue>FUNCTIONALERROR000</originalAttributeValue>
            </FunctionalError>

          when(
            mockNotificationService.sendIE906Notification(
              eqTo(notificationData),
              eqTo(correlationId),
              argThat(xml => XmlOps.normalize(xml) == XmlOps.normalize(IE906ErrorXml))
            )(any[HeaderCarrier])
          )
            .thenReturn(Future.successful(()))

          val xmlBody: Elem =
            <AESDigitalNotification>
              <Header>
                <messageSender>{eoriNumber}</messageSender>
              </Header>
              <Body>
                <MRN>{mrn}</MRN>
                <GoodsShipment>
                  <Consignment>
                    <ReferenceNumberUCRID>FUNCTIONALERROR000</ReferenceNumberUCRID>
                    <parentUCRID>ACKDIVERSIONBB</parentUCRID>
                  </Consignment>
                </GoodsShipment>
              </Body>
            </AESDigitalNotification>

          val requestWithHeaders: FakeRequest[AnyContentAsXml] =
            fakeRequest
              .withHeaders(requiredHeaders.toSeq: _*)
              .withHeaders("x-correlation-id" -> correlationId)
              .withXmlBody(xmlBody)

          val result: Future[Result] = controller.message()(requestWithHeaders)

          status(result)         shouldBe Status.NO_CONTENT
          contentType(result)    shouldBe None
          contentAsBytes(result) shouldBe ByteString.empty
        }
      }
    }

    "return 400 when required headers are missing" in new Setup {
      val requestWithHeaders = fakeRequest.withHeaders(("some-header", "header-val"), ("another-header", "another"))
      val result             = controller.message()(requestWithHeaders)
      status(result) shouldBe Status.BAD_REQUEST
    }
  }

  trait MrnSetup:
    val requiredHeaders = Map(
      "x-forwarded-host" -> "*",
      "x-correlation-id" -> "*",
      "date"             -> "*",
      "authorization"    -> "Bearer *",
      "content-type"     -> "application/xml",
      "accept"           -> "application/xml",
      "message-type"     -> "aesIE507Request"
    )
    when(mockAppConfig.requiredHeaders).thenReturn(requiredHeaders)
    val validatedRequestAction = ValidatedRequestAction(mock[BodyParsers.Default], mockAppConfig)
    when(
      mockNotificationService.sendAckNotification(
        any[NotificationData],
        any[ActionCode],
        any[String]
      )(any[HeaderCarrier])
    ).thenReturn(Future.successful(()))
    val controller = new MessageController(Helpers.stubControllerComponents(), mockNotificationService, validatedRequestAction)

  "MRN error responses" - {
    "return correct XML error response when MRN ends in A0" in new MrnSetup:
      val request = TestData.requestWithMrn("24AB1234567890A0")
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
      val request = TestData.requestWithMrn("24AB1234567890A2")
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
