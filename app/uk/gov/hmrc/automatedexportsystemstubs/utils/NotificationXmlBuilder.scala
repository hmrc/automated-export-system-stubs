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

package uk.gov.hmrc.automatedexportsystemstubs.utils

import play.api.{Logger, Logging}
import uk.gov.hmrc.automatedexportsystemstubs.models.{ActionCode, NotificationData}

import scala.xml.*

object NotificationXmlBuilder extends Logging:

  override val logger = Logger(this.getClass)

  def parseIncomingAckXml(correlationId: String, xml: NodeSeq): NotificationData =
    val mrn  = (xml \\ "MRN").text.trim
    val eori = (xml \\ "messageSender").text.trim
    if mrn.isEmpty || eori.isEmpty then throw new IllegalArgumentException("Missing required fields: MRN, EORI")
    NotificationData(eori, correlationId, mrn)

  private def buildEnvelope(
    data:            NotificationData,
    currentDateTime: String,
    messageType:     String,
    bodyNodes:       NodeSeq
  ): Elem =
    <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
      <Header>
        <messageSender>NECA.XI</messageSender>
        <messageRecipient>{data.eori}</messageRecipient>
        <preparationDateTime>{currentDateTime}</preparationDateTime>
        <messageIdentification>{data.correlationId}</messageIdentification>
        <messageType>{messageType}</messageType>
        <correlationIdentifier>{data.correlationId}</correlationIdentifier>
      </Header>
      <Body>
        {bodyNodes}
      </Body>
    </AESDigitalNotification>

  def buildAckResponseXml(
    data:            NotificationData,
    actionCode:      ActionCode,
    currentDateTime: String
  ): Elem =
    buildEnvelope(
      data,
      currentDateTime,
      messageType = "ACK",
      bodyNodes = Seq(
        <messageCode>CC507C</messageCode>,
        <actionCode>{actionCode.value}</actionCode>,
        <MRN>{data.mrn}</MRN>
      )
    )

  def buildIE906ResponseXml(
    data:             NotificationData,
    currentDateTime:  String,
    functionalErrors: NodeSeq
  ): Elem =
    buildEnvelope(
      data,
      currentDateTime,
      messageType = "CD906C",
      bodyNodes = Seq(
        <messageCode>CC507C</messageCode>,
        <MRN>{data.mrn}</MRN>
      ) ++ functionalErrors
    )

  def buildIE917ResponseXml(
    data:            NotificationData,
    currentDateTime: String,
    xmlErrors:       NodeSeq
  ): Elem =
    buildEnvelope(
      data,
      currentDateTime,
      messageType = "CD917C",
      bodyNodes = Seq(
        <messageCode>CC507C</messageCode>,
        <MRN>
            {data.mrn}
          </MRN>
      ) ++ xmlErrors
    )

  def xmlToString(xml: Elem): String = xml.toString()
