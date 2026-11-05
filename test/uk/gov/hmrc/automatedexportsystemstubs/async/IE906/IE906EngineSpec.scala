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

package uk.gov.hmrc.automatedexportsystemstubs.async.IE906

import uk.gov.hmrc.automatedexportsystemstubs.helpers.{BaseSpec, XmlOps}

import scala.xml.Elem

class IE906EngineSpec extends BaseSpec:
  "IE906Engine.toXml" - {

    "build one error node " in {
      val m = IE906Engine.MatchResult("90", "/Body/MRN", "26GB123456789ABCDEB0")

      val functionalError: Elem =
        <FunctionalError>
          <errorPointer>/Body/MRN</errorPointer>
          <errorCode>90</errorCode>
          <errorReason>ERR02</errorReason>
          <originalAttributeValue>26GB123456789ABCDEB0</originalAttributeValue>
        </FunctionalError>

      val xml: Elem = IE906Engine.toFunctionalError(m)

      XmlOps.normalize(xml) shouldBe XmlOps.normalize(functionalError)
    }

    "IE906Engine.allMatches -> toFunctionalError" - {

      "create multiple XMLError nodes when multiple rules match" in {
        val input: Elem =
          <CC507C>
            <Body>
              <ExportOperation>
                <MRN>26GB123456789ABCDEB0</MRN>
              </ExportOperation>
              <GoodsShipment>
                <Consignment>
                  <ReferenceNumberUCRID>XXX001</ReferenceNumberUCRID>
                </Consignment>
              </GoodsShipment>
            </Body>
          </CC507C>

        val functionalError1: Elem =
          <FunctionalError>
            <errorPointer>/CC507C/Body/ExportOperation/MRN</errorPointer>
            <errorCode>90</errorCode>
            <errorReason>ERR02</errorReason>
            <originalAttributeValue>26GB123456789ABCDEB0</originalAttributeValue>
          </FunctionalError>

        val functionalError2: Elem =
          <FunctionalError>
            <errorPointer>/CC507C/Body/GoodsShipment/Consignment/ReferenceNumberUCRID</errorPointer>
            <errorCode>96</errorCode>
            <errorReason>ERR02</errorReason>
            <originalAttributeValue>XXX001</originalAttributeValue>
          </FunctionalError>

        val functionalErrors: Seq[Elem] = Seq(functionalError1, functionalError2)

        val errors: Seq[Elem] =
          IE906Engine.allMatches(input).map(IE906Engine.toFunctionalError)

        errors.map(XmlOps.normalize) shouldBe functionalErrors.map(XmlOps.normalize)
      }

      "return no XMLError nodes when no rules match" in {
        val input: Elem =
          <CC507C>
            <Body>
              <MRN>26GB123456789ABCDE00</MRN>
            </Body>
          </CC507C>

        val errors: Seq[Elem] = IE906Engine.allMatches(input).map(IE906Engine.toFunctionalError)

        errors shouldBe Nil
      }
    }

  }
