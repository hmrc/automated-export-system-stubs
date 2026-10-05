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

package uk.gov.hmrc.automatedexportsystemstubs.helpers.controllers

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import play.api.inject
import play.api.inject.Binding
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.automatedexportsystemstubs.helpers.BaseISpec
import uk.gov.hmrc.automatedexportsystemstubs.models.ActionCode

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*
import scala.xml.{Elem, NodeSeq}

class MessageControllerISpec extends BaseISpec:

  trait Setup:
    val endpoint: String = "/cds/aesIE507Request/v1"

    val correlationId: String = "correlationId"
    val eoriNumber:    String = "GB123456789000"

    val mrn:                      String  = "26GB123456789ABCDEX9"
    val notificationsBearerToken: String  = "notifications-token"
    val instant:                  Instant = Instant.parse("2026-10-01T00:00:00.000Z")
    val rfc1123DateTime:          String  = "Thu, 01 Oct 2026 00:00:00 UTC"

    val validHeaders: Seq[(String, String)] = Seq(
      "Authorization"    -> "Bearer token",
      "x-correlation-id" -> correlationId,
      "accept"           -> Helpers.XML,
      "content-type"     -> Helpers.XML,
      "date"             -> rfc1123DateTime,
      "x-message-type"   -> "aesIE507Request",
      "x-forwarded-host" -> "some-host"
    )

    def notificationPostRequestMappingBuilder(notificationPayloadXml: Elem): MappingBuilder =
      post(urlEqualTo("/automated-export-system-notifications/notification"))
        .withHeader("Authorization", equalTo(s"Bearer $notificationsBearerToken"))
        .withHeader("Content-Type", equalTo(Helpers.XML))
        .withHeader("X-Correlation-Id", equalTo(correlationId))
        .withRequestBody(equalToXml(notificationPayloadXml.toString))
  end Setup

  object Setup extends Setup

  override def config: Map[String, Any] =
    super.config ++ Map("microservice.services.aes-notifications.bearer-token" -> Setup.notificationsBearerToken)

  override def bindingOverrides: Seq[Binding[_]] =
    super.bindingOverrides ++ Seq(
      inject.bind[Clock].toInstance(Clock.fixed(Setup.instant, ZoneOffset.UTC))
    )

  "POST /cds/aesIE507Request/v1" - {

    "route exists" in new Setup {
      route(app, FakeRequest(POST, endpoint)).isDefined shouldBe true
    }

    "returns 400 when no XML body" in new Setup {
      val request = FakeRequest(POST, endpoint)
        .withHeaders(validHeaders*)

      val result = route(app, request).value
      status(result) shouldBe BAD_REQUEST
    }

    "returns sync 401 when MRN ends with A0" in new Setup {
      val body =
        """<AESDigitalNotification>
          |  <Header><messageSender>GB123</messageSender></Header>
          |  <Body><MRN>26GB123456789ABCDE0A0</MRN></Body>
          |</AESDigitalNotification>""".stripMargin

      val request = FakeRequest(POST, endpoint)
        .withHeaders(validHeaders*)
        .withTextBody(body)

      val result = route(app, request).value
      status(result)                                    shouldBe UNAUTHORIZED
      contentType(result)                               shouldBe Some("application/xml")
      (contentAsString(result) contains "UNAUTHORIZED") shouldBe true

    }

    "returns sync 404 when MRN ends with A1" in new Setup {
      val body =
        """<AESDigitalNotification>
          |  <Header><messageSender>GB123</messageSender></Header>
          |  <Body><MRN>26GB123456789ABCDE0A1</MRN></Body>
          |</AESDigitalNotification>""".stripMargin

      val result = route(
        app,
        FakeRequest(POST, endpoint)
          .withHeaders(validHeaders*)
          .withTextBody(body)
      ).value

      status(result) shouldBe NOT_FOUND
    }

    "returns 204 for async IE906 path (e.g. MRN ends B0 -> code 90 matched and forwarded)" in new Setup {
      val IE906ErrorXml: Elem =
        <FunctionalError>
          <errorPointer>Body.ExportOperation.MRN</errorPointer>
          <errorCode>90</errorCode>
          <errorReason>ERR02</errorReason>
          <originalAttributeValue>MRN-B0</originalAttributeValue>
        </FunctionalError>

      val requestXml: Elem =
        <AESDigitalNotification>
          <Header>
            <messageSender>{eoriNumber}</messageSender>
          </Header>
          <Body>
            <ExportOperation>
              <MRN>MRN-B0</MRN>
            </ExportOperation>
          </Body>
        </AESDigitalNotification>

      val notificationPayloadIE906ErrorXml: Elem =
        <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
          <Header>
            <messageSender>NECA.XI</messageSender>
            <messageRecipient>{eoriNumber}</messageRecipient>
            <preparationDateTime>{rfc1123DateTime}</preparationDateTime>
            <messageIdentification>{correlationId}</messageIdentification>
            <messageType>CD906C</messageType>
            <correlationIdentifier>{correlationId}</correlationIdentifier>
          </Header>
          <Body>
            <messageCode>CC507C</messageCode>
            <MRN>MRN-B0</MRN>
            {IE906ErrorXml}
          </Body>
        </AESDigitalNotification>

      stubFor(
        notificationPostRequestMappingBuilder(notificationPayloadIE906ErrorXml)
          .willReturn(
            aResponse()
              .withStatus(Helpers.NO_CONTENT)
          )
      )

      val request: FakeRequest[NodeSeq] =
        FakeRequest(Helpers.POST, endpoint)
          .withHeaders(validHeaders*)
          .withBody(requestXml)

      val result: Future[Result] = Helpers.route(app, request).value

      WireMock.findUnmatchedRequests().asScala

      Helpers.status(result) shouldBe Helpers.NO_CONTENT
    }

    "returns 204 for async IE917 path (e.g. office of exit reference number ends 000 -> code 12 matched and forwarded)" in new Setup {
      val IE917ErrorXml: Elem =
        <XmlError>
          <errorPointer>Body.CustomsOfficeOExitActual.referenceNumber</errorPointer>
          <errorCode>12</errorCode>
          <errorText>ERR02</errorText>
          <originalAttributeValue>some-reference-000</originalAttributeValue>
        </XmlError>

      val requestXml: Elem =
        <AESDigitalNotification>
          <Header>
            <messageSender>{eoriNumber}</messageSender>
          </Header>
          <Body>
            <CustomsOfficeOExitActual>
              <referenceNumber>some-reference-000</referenceNumber>
            </CustomsOfficeOExitActual>
            <ExportOperation>
              <MRN>{mrn}</MRN>
            </ExportOperation>
          </Body>
        </AESDigitalNotification>

      val notificationPayloadAcceptedXml: Elem =
        <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
          <Header>
            <messageSender>NECA.XI</messageSender>
            <messageRecipient>{eoriNumber}</messageRecipient>
            <preparationDateTime>{rfc1123DateTime}</preparationDateTime>
            <messageIdentification>{correlationId}</messageIdentification>
            <messageType>CD917C</messageType>
            <correlationIdentifier>{correlationId}</correlationIdentifier>
          </Header>
          <Body>
            <messageCode>CC507C</messageCode>
            <MRN>{mrn}</MRN>
            {IE917ErrorXml}
          </Body>
        </AESDigitalNotification>

      stubFor(
        notificationPostRequestMappingBuilder(notificationPayloadAcceptedXml)
          .willReturn(
            aResponse()
              .withStatus(Helpers.NO_CONTENT)
          )
      )

      val request: FakeRequest[NodeSeq] =
        FakeRequest(Helpers.POST, endpoint)
          .withHeaders(validHeaders*)
          .withBody(requestXml)

      val result: Future[Result] = Helpers.route(app, request).value

      WireMock.findUnmatchedRequests().asScala

      Helpers.status(result) shouldBe Helpers.NO_CONTENT
    }

    "returns 204 for normal ACK path when no sync/IE906 rule matches" in new Setup {
      val requestXml: Elem =
        <AESDigitalNotification>
          <Header>
            <messageSender>{eoriNumber}</messageSender>
          </Header>
          <Body>
            <ExportOperation>
              <MRN>{mrn}</MRN>
            </ExportOperation>
          </Body>
        </AESDigitalNotification>

      val notificationPayloadAcceptedXml: Elem =
        <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
          <Header>
            <messageSender>NECA.XI</messageSender>
            <messageRecipient>{eoriNumber}</messageRecipient>
            <preparationDateTime>{rfc1123DateTime}</preparationDateTime>
            <messageIdentification>{correlationId}</messageIdentification>
            <messageType>ACK</messageType>
            <correlationIdentifier>{correlationId}</correlationIdentifier>
          </Header>
          <Body>
            <messageCode>CC507C</messageCode>
            <actionCode>{ActionCode.Accepted.value}</actionCode>
            <MRN>{mrn}</MRN>
          </Body>
        </AESDigitalNotification>

      stubFor(
        notificationPostRequestMappingBuilder(notificationPayloadAcceptedXml)
          .willReturn(
            aResponse()
              .withStatus(Helpers.NO_CONTENT)
          )
      )

      val request: FakeRequest[NodeSeq] =
        FakeRequest(Helpers.POST, endpoint)
          .withHeaders(validHeaders*)
          .withBody(requestXml)

      val result: Future[Result] = Helpers.route(app, request).value

      WireMock.findUnmatchedRequests().asScala

      Helpers.status(result) shouldBe Helpers.NO_CONTENT
    }

    "returns 204 when ACK Diversion" - {

      "and followed by an ACK Accepted" in new Setup {
        val requestXml: Elem =
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

        val notificationPayloadDiversionXml: Elem =
          <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            <Header>
              <messageSender>NECA.XI</messageSender>
              <messageRecipient>{eoriNumber}</messageRecipient>
              <preparationDateTime>{rfc1123DateTime}</preparationDateTime>
              <messageIdentification>{correlationId}</messageIdentification>
              <messageType>ACK</messageType>
              <correlationIdentifier>{correlationId}</correlationIdentifier>
            </Header>
            <Body>
              <messageCode>CC507C</messageCode>
              <actionCode>{ActionCode.Diversion.value}</actionCode>
              <MRN>{mrn}</MRN>
            </Body>
          </AESDigitalNotification>

        stubFor(
          notificationPostRequestMappingBuilder(notificationPayloadDiversionXml)
            .willReturn(
              aResponse()
                .withStatus(Helpers.NO_CONTENT)
            )
        )

        val notificationPayloadAcceptedXml: Elem =
          <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            <Header>
              <messageSender>NECA.XI</messageSender>
              <messageRecipient>{eoriNumber}</messageRecipient>
              <preparationDateTime>{rfc1123DateTime}</preparationDateTime>
              <messageIdentification>{correlationId}</messageIdentification>
              <messageType>ACK</messageType>
              <correlationIdentifier>{correlationId}</correlationIdentifier>
            </Header>
            <Body>
              <messageCode>CC507C</messageCode>
              <actionCode>{ActionCode.Accepted.value}</actionCode>
              <MRN>{mrn}</MRN>
            </Body>
          </AESDigitalNotification>

        stubFor(
          notificationPostRequestMappingBuilder(notificationPayloadAcceptedXml)
            .willReturn(
              aResponse()
                .withStatus(Helpers.NO_CONTENT)
            )
        )

        val request: FakeRequest[NodeSeq] =
          FakeRequest(Helpers.POST, endpoint)
            .withHeaders(validHeaders*)
            .withBody(requestXml)

        val result: Future[Result] = Helpers.route(app, request).value

        WireMock.findUnmatchedRequests().asScala

        Helpers.status(result) shouldBe Helpers.NO_CONTENT
      }

      "and followed by an IE917 error" in new Setup {
        val IE917ErrorXml: Elem =
          <XmlError>
            <errorPointer>Body.CustomsOfficeOExitActual.referenceNumber</errorPointer>
            <errorCode>12</errorCode>
            <errorText>ERR02</errorText>
            <originalAttributeValue>XMLERROR000</originalAttributeValue>
          </XmlError>

        val requestXml: Elem =
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

        val notificationPayloadDiversionXml: Elem =
          <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            <Header>
              <messageSender>NECA.XI</messageSender>
              <messageRecipient>{eoriNumber}</messageRecipient>
              <preparationDateTime>{rfc1123DateTime}</preparationDateTime>
              <messageIdentification>{correlationId}</messageIdentification>
              <messageType>ACK</messageType>
              <correlationIdentifier>{correlationId}</correlationIdentifier>
            </Header>
            <Body>
              <messageCode>CC507C</messageCode>
              <actionCode>{ActionCode.Diversion.value}</actionCode>
              <MRN>{mrn}</MRN>
            </Body>
          </AESDigitalNotification>

        stubFor(
          notificationPostRequestMappingBuilder(notificationPayloadDiversionXml)
            .willReturn(
              aResponse()
                .withStatus(Helpers.NO_CONTENT)
            )
        )

        val notificationPayloadIE917ErrorXml: Elem =
          <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            <Header>
              <messageSender>NECA.XI</messageSender>
              <messageRecipient>{eoriNumber}</messageRecipient>
              <preparationDateTime>{rfc1123DateTime}</preparationDateTime>
              <messageIdentification>{correlationId}</messageIdentification>
              <messageType>CD917C</messageType>
              <correlationIdentifier>{correlationId}</correlationIdentifier>
            </Header>
            <Body>
              <messageCode>CC507C</messageCode>
              <MRN>{mrn}</MRN>
              {IE917ErrorXml}
            </Body>
          </AESDigitalNotification>

        stubFor(
          notificationPostRequestMappingBuilder(notificationPayloadIE917ErrorXml)
            .willReturn(
              aResponse()
                .withStatus(Helpers.NO_CONTENT)
            )
        )

        val request: FakeRequest[NodeSeq] =
          FakeRequest(Helpers.POST, endpoint)
            .withHeaders(validHeaders*)
            .withBody(requestXml)

        val result: Future[Result] = Helpers.route(app, request).value

        WireMock.findUnmatchedRequests().asScala

        Helpers.status(result) shouldBe Helpers.NO_CONTENT
      }

      "and followed by an IE906 error" in new Setup {
        val IE906ErrorXml: Elem =
          <FunctionalError>
            <errorPointer>Body.GoodsShipment.Consignment.ReferenceNumberUCRID</errorPointer>
            <errorCode>8</errorCode>
            <errorReason>ERR02</errorReason>
            <originalAttributeValue>FUNCTIONALERROR000</originalAttributeValue>
          </FunctionalError>

        val requestXml: Elem =
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

        val notificationPayloadDiversionXml: Elem =
          <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            <Header>
              <messageSender>NECA.XI</messageSender>
              <messageRecipient>{eoriNumber}</messageRecipient>
              <preparationDateTime>{rfc1123DateTime}</preparationDateTime>
              <messageIdentification>{correlationId}</messageIdentification>
              <messageType>ACK</messageType>
              <correlationIdentifier>{correlationId}</correlationIdentifier>
            </Header>
            <Body>
              <messageCode>CC507C</messageCode>
              <actionCode>{ActionCode.Diversion.value}</actionCode>
              <MRN>{mrn}</MRN>
            </Body>
          </AESDigitalNotification>

        stubFor(
          notificationPostRequestMappingBuilder(notificationPayloadDiversionXml)
            .willReturn(
              aResponse()
                .withStatus(Helpers.NO_CONTENT)
            )
        )

        val notificationPayloadIE916ErrorXml: Elem =
          <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            <Header>
              <messageSender>NECA.XI</messageSender>
              <messageRecipient>{eoriNumber}</messageRecipient>
              <preparationDateTime>{rfc1123DateTime}</preparationDateTime>
              <messageIdentification>{correlationId}</messageIdentification>
              <messageType>CD906C</messageType>
              <correlationIdentifier>{correlationId}</correlationIdentifier>
            </Header>
            <Body>
              <messageCode>CC507C</messageCode>
              <MRN>{mrn}</MRN>
              {IE906ErrorXml}
            </Body>
          </AESDigitalNotification>

        stubFor(
          notificationPostRequestMappingBuilder(notificationPayloadIE916ErrorXml)
            .willReturn(
              aResponse()
                .withStatus(Helpers.NO_CONTENT)
            )
        )

        val request: FakeRequest[NodeSeq] =
          FakeRequest(Helpers.POST, endpoint)
            .withHeaders(validHeaders*)
            .withBody(requestXml)

        val result: Future[Result] = Helpers.route(app, request).value

        WireMock.findUnmatchedRequests().asScala

        Helpers.status(result) shouldBe Helpers.NO_CONTENT
      }
    }
  }
