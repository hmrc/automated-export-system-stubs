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

package uk.gov.hmrc.automatedexportsystemstubs.errors

import uk.gov.hmrc.automatedexportsystemstubs.errors.IE917.IE917Engine
import uk.gov.hmrc.automatedexportsystemstubs.helpers.BaseSpec

import scala.xml.Elem

class IE917EngineSpec extends BaseSpec:
  "IE917Engine.toXml" - {
    "build one error node " in {
      val m = IE917Engine.MatchResult("12", "Body/CustomsOfficeOExitActual/referenceNumber", "26GB123456789ABCD000")

      val xml = IE917Engine.toXmlError(m)

      (xml \ "errorPointer").text           shouldBe "Body/CustomsOfficeOExitActual/referenceNumber"
      (xml \ "errorCode").text              shouldBe "12"
      (xml \ "errorText").text              shouldBe "ERR02"
      (xml \ "originalAttributeValue").text shouldBe "26GB123456789ABCD000"
    }

    "IE917Engine.allMatches -> toXMLError" - {
      "create multiple XMLError nodes when multiple rules match" in {
        val input: Elem =
          <AESDigitalNotification>
            <Body>
              <CustomsOfficeOExitActual><referenceNumber>26GB123456789ABCD000</referenceNumber></CustomsOfficeOExitActual>
              <ExportOperation><MRN>26GB123456789ABCDEB0</MRN></ExportOperation>
              <GoodsShipment>
                <Consignment><parentUCRID>XXX033</parentUCRID></Consignment>
              </GoodsShipment>
            </Body>
          </AESDigitalNotification>

        val errors: Seq[Elem] =
          IE917Engine.allMatches(input).map(IE917Engine.toXmlError)

        errors.size shouldBe 2

        val codes = errors.map(e => (e \ "errorCode").text)
        codes should contain("12")
        codes should contain("52")
      }

      "return no XMLError nodes when no rules match" in {
        val input: Elem =
          <AESDigitalNotification>
            <Body>
              <MRN>26GB123456789ABCDE00</MRN>
            </Body>
          </AESDigitalNotification>

        val errors = IE917Engine.allMatches(input).map(IE917Engine.toXmlError)
        errors shouldBe empty
      }
    }

  }
