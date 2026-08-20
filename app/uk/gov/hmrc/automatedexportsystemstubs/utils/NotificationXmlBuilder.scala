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

import scala.xml._
import play.api.{Logger, Logging}
import scala.xml.Elem

object NotificationXmlBuilder extends Logging:

  override val logger = Logger(this.getClass)

  case class NotificationData(messageRecipient: String, mrn: String)

  def parseIncomingAckXml(correlationId: String, xml: scala.xml.Elem): AckNotification = {
    val mrn  = (xml \\ "MRN").text.trim
    val eori = (xml \\ "messageSender").text.trim

    if (mrn.isEmpty || eori.isEmpty)
      throw new IllegalArgumentException("Missing required fields: MRN, EORI")

    AckNotification(eori, correlationId, mrn)
  }

  def buildAckResponseXml(
    data:            AckNotification,
    currentDateTime: String
  ): Elem = {
    <AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
      <Header>
        <messageSender>NECA.XI</messageSender>
        <messageRecipient>{data.eori}</messageRecipient>
        <preparationDateTime>{currentDateTime}</preparationDateTime>
        <messageIdentification>{data.correlationId}</messageIdentification>
        <messageType>ACK</messageType>
        <correlationIdentifier>{data.correlationId}</correlationIdentifier>
      </Header>
      <Body>
        <messageCode>CC507C</messageCode>
        <actionCode>1</actionCode>
        <MRN>{data.mrn}</MRN>
      </Body>
    </AESDigitalNotification>
  }

  def xmlToString(xml: Elem): String =
    xml.toString()
