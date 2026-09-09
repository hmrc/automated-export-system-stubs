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

import uk.gov.hmrc.automatedexportsystemstubs.models.AckNotification

import scala.xml.*
import play.api.{Logger, Logging}
import scala.xml.Elem

object NotificationXmlBuilder extends Logging:

  override val logger = Logger(this.getClass)

  def parseIncomingAckXml(correlationId: String, xml: scala.xml.Elem): AckNotification =
    val mrn  = (xml \\ "MRN").text.trim
    val eori = (xml \\ "messageSender").text.trim
    if mrn.isEmpty || eori.isEmpty then throw new IllegalArgumentException("Missing required fields: MRN, EORI")
    AckNotification(eori, correlationId, mrn)

  private def buildEnvelope(
    data:            AckNotification,
    currentDateTime: String,
    messageType:     String,
    bodyNodes:       Seq[scala.xml.Node]
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
      <Body>{bodyNodes}</Body>
    </AESDigitalNotification>

  def buildAckResponseXml(data: AckNotification, currentDateTime: String): Elem =
    buildEnvelope(
      data,
      currentDateTime,
      messageType = "ACK",
      bodyNodes = Seq(
        <messageCode>CC507C</messageCode>,
        <actionCode>1</actionCode>,
        <MRN>{data.mrn}</MRN>
      )
    )

  def buildIE906ResponseXml(
    data:             AckNotification,
    currentDateTime:  String,
    functionalErrors: List[Elem]
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
    data:            AckNotification,
    currentDateTime: String,
    xmlErrors:       List[Elem]
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
