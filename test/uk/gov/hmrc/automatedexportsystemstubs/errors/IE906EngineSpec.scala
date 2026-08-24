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

import uk.gov.hmrc.automatedexportsystemstubs.helpers.BaseSpec

import scala.xml.Elem

class IE906EngineSpec extends BaseSpec:
  "IE906Engine.toXml" - {
    "build one error node " in {
      val m = Ie906Engine.MatchResult("90", "/Body/MRN", "26GB123456789ABCDEB0")

      val xml = Ie906Engine.toXmlError(m)

      (xml \ "errorPointer").text           shouldBe "/Body/MRN"
      (xml \ "errorCode").text              shouldBe "90"
      (xml \ "errorText").text              shouldBe "ERR02"
      (xml \ "originalAttributeValue").text shouldBe "26GB123456789ABCDEB0"
    }

    "IE906Engine.allMatches -> toXmlError" - {
      "create multiple XMLError nodes when multiple rules match" in {
        val input: Elem =
          <AESDigitalNotification>
            <Body>
              <ExportOperation><MRN>26GB123456789ABCDEB0</MRN></ExportOperation>
              <GoodsShipment><Consignment><ReferenceNumberUCRID>XXX001</ReferenceNumberUCRID></Consignment></GoodsShipment>
            </Body>
          </AESDigitalNotification>

        val errors: Seq[Elem] =
          Ie906Engine.allMatches(input).map(Ie906Engine.toXmlError)

        errors.size shouldBe 2

        val codes = errors.map(e => (e \ "errorCode").text)
        codes should contain("90")
        codes should contain("96")
      }

      "return no XMLError nodes when no rules match" in {
        val input: Elem =
          <AESDigitalNotification>
            <Body>
              <MRN>26GB123456789ABCDE00</MRN>
            </Body>
          </AESDigitalNotification>

        val errors = Ie906Engine.allMatches(input).map(Ie906Engine.toXmlError)
        errors shouldBe empty
      }
    }

  }
