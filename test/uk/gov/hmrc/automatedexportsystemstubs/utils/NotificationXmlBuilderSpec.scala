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

import uk.gov.hmrc.automatedexportsystemstubs.helpers.BaseSpec
import uk.gov.hmrc.automatedexportsystemstubs.models.AckNotification

class NotificationXmlBuilderSpec extends BaseSpec {

  "parseIncomingAckXml" - {

    "successfully parse valid XML and extract MRN and EORI" in {
      val correlationId = "test-correlation-123"
      val xmlPayload    =
        """<?xml version="1.0" encoding="UTF-8"?>
            |<AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            |  <Header>
            |    <messageSender>GB123456789000</messageSender>
            |  </Header>
            |  <Body>
            |    <MRN>26GB123456789ABCDE1</MRN>
            |  </Body>
            |</AESDigitalNotification>""".stripMargin

      val result = NotificationXmlBuilder.parseIncomingAckXml(correlationId, xmlPayload)

      result.eori          shouldBe "GB123456789000"
      result.mrn           shouldBe "26GB123456789ABCDE1"
      result.correlationId shouldBe correlationId
    }

    "throw exception when MRN is missing" in {
      val correlationId = "test-correlation-123"
      val xmlPayload    =
        """<?xml version="1.0" encoding="UTF-8"?>
            |<AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            |  <Header>
            |    <messageSender>GB123456789000</messageSender>
            |  </Header>
            |  <Body>
            |  </Body>
            |</AESDigitalNotification>""".stripMargin

      val exception = intercept[RuntimeException] {
        NotificationXmlBuilder.parseIncomingAckXml(correlationId, xmlPayload)
      }

      exception.getMessage should include("Missing required fields: MRN, EORI")
    }

    "throw exception when EORI (messageSender) is missing" in {
      val correlationId = "test-correlation-123"
      val xmlPayload    =
        """<?xml version="1.0" encoding="UTF-8"?>
            |<AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            |  <Header>
            |  </Header>
            |  <Body>
            |    <MRN>26GB123456789ABCDE1</MRN>
            |  </Body>
            |</AESDigitalNotification>""".stripMargin

      val exception = intercept[RuntimeException] {
        NotificationXmlBuilder.parseIncomingAckXml(correlationId, xmlPayload)
      }

      exception.getMessage should include("Missing required fields: MRN, EORI")
    }

    "throw exception for invalid XML" in {
      val correlationId = "test-correlation-123"
      val invalidXml    = "<invalid>xml"

      val exception = intercept[RuntimeException] {
        NotificationXmlBuilder.parseIncomingAckXml(correlationId, invalidXml)
      }

      exception.getMessage should include("Failed to parse XML")
    }

    "find MRN and EORI at any depth in XML" in {
      val correlationId = "test-correlation-123"
      val xmlPayload    =
        """<?xml version="1.0" encoding="UTF-8"?>
            |<AESDigitalNotification xmlns="http://www.hmrc.gsi.gov.uk/eis">
            |  <Wrapper>
            |    <Header>
            |      <messageSender>GB987654321000</messageSender>
            |    </Header>
            |    <Data>
            |      <Body>
            |        <MRN>26GB987654321ZYXWV1</MRN>
            |      </Body>
            |    </Data>
            |  </Wrapper>
            |</AESDigitalNotification>""".stripMargin

      val result = NotificationXmlBuilder.parseIncomingAckXml(correlationId, xmlPayload)

      result.eori shouldBe "GB987654321000"
      result.mrn  shouldBe "26GB987654321ZYXWV1"
    }
  }

  "buildAckResponseXml" - {

    "build valid response XML with all required elements" in {
      val ackNotification = AckNotification("GB123456789000", "corr-123", "26GB123456789ABCDE1")
      val currentDateTime = "2026-08-10T14:30:00"

      val result = NotificationXmlBuilder.buildAckResponseXml(ackNotification, currentDateTime)

      (result \\ "messageSender").text         shouldBe "NECA.XI"
      (result \\ "messageRecipient").text      shouldBe "GB123456789000"
      (result \\ "preparationDateTime").text   shouldBe currentDateTime
      (result \\ "messageIdentification").text shouldBe "corr-123"
      (result \\ "messageType").text           shouldBe "ACK"
      (result \\ "correlationIdentifier").text shouldBe "corr-123"
      (result \\ "messageCode").text           shouldBe "CC507C"
      (result \\ "actionCode").text            shouldBe "1"
      (result \\ "MRN").text                   shouldBe "26GB123456789ABCDE1"
    }

    "include correct namespace" in {
      val ackNotification = AckNotification("GB123456789000", "corr-123", "26GB123456789ABCDE1")
      val currentDateTime = "2026-08-10T14:30:00"

      val result    = NotificationXmlBuilder.buildAckResponseXml(ackNotification, currentDateTime)
      val xmlString = result.toString()

      xmlString should include("""xmlns="http://www.hmrc.gsi.gov.uk/eis"""")
    }
  }

  "xmlToString" - {
    "convert XML element to string" in {
      val xml = <test>
          <value>hello</value>
        </test>
      val result = NotificationXmlBuilder.xmlToString(xml)

      result should include("<test>")
      result should include("<value>hello</value>")
      result should include("</test>")
    }
  }
}
