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

package uk.gov.hmrc.automatedexportsystemstubs.async.IE917

import uk.gov.hmrc.automatedexportsystemstubs.helpers.{BaseSpec, XmlOps}

import scala.xml.Elem

class IE917EngineSpec extends BaseSpec:
  "IE917Engine.toXml" - {

    "build one error node " in {
      val m = IE917Engine.MatchResult("12", "Body/CustomsOfficeOfExitActual/referenceNumber", "26GB123456789ABCD000")

      val xmlError: Elem =
        <XmlError>
          <errorPointer>Body/CustomsOfficeOfExitActual/referenceNumber</errorPointer>
          <errorCode>12</errorCode>
          <errorText>ERR02</errorText>
          <originalAttributeValue>26GB123456789ABCD000</originalAttributeValue>
        </XmlError>

      val xml = IE917Engine.toXmlError(m)

      XmlOps.normalize(xml) shouldBe XmlOps.normalize(xmlError)
    }

    "IE917Engine.allMatches -> toXMLError" - {

      "create multiple XMLError nodes when multiple rules match" in {
        val input: Elem =
          <CC507C>
            <Body>
              <CustomsOfficeOfExitActual>
                <referenceNumber>26GB123456789ABCD000</referenceNumber>
              </CustomsOfficeOfExitActual>
              <ExportOperation>
                <MRN>26GB123456789ABCDEB0</MRN>
              </ExportOperation>
              <GoodsShipment>
                <Consignment>
                  <parentUCRID>XXX033</parentUCRID>
                </Consignment>
              </GoodsShipment>
            </Body>
          </CC507C>

        val xmlError1: Elem =
          <XmlError>
            <errorPointer>/CC507C/Body/CustomsOfficeOfExitActual/referenceNumber</errorPointer>
            <errorCode>12</errorCode>
            <errorText>ERR02</errorText>
            <originalAttributeValue>26GB123456789ABCD000</originalAttributeValue>
          </XmlError>

        val xmlError2: Elem =
          <XmlError>
            <errorPointer>/CC507C/Body/GoodsShipment/Consignment/parentUCRID</errorPointer>
            <errorCode>52</errorCode>
            <errorText>ERR02</errorText>
            <originalAttributeValue>XXX033</originalAttributeValue>
          </XmlError>

        val xmlErrors = Seq(xmlError1, xmlError2)

        val errors: Seq[Elem] =
          IE917Engine.allMatches(input).map(IE917Engine.toXmlError)

        errors.map(XmlOps.normalize) shouldBe xmlErrors.map(XmlOps.normalize)
      }

      "return no XMLError nodes when no rules match" in {
        val input: Elem =
          <CC507C>
            <Body>
              <MRN>26GB123456789ABCDE00</MRN>
            </Body>
          </CC507C>

        val errors: Seq[Elem] = IE917Engine.allMatches(input).map(IE917Engine.toXmlError)

        errors shouldBe Nil
      }
    }

  }
