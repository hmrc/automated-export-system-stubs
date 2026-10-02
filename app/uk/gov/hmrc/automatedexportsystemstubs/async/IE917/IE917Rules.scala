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

import uk.gov.hmrc.automatedexportsystemstubs.utils.XmlRequestBodyExtractor.*
import uk.gov.hmrc.automatedexportsystemstubs.utils.XmlRequestHelper.*

object IE917Rules:
  val all: List[XmlRule] = List(
    XmlRule("12", extractCustomsOfficeOExitReferenceNumber, endsWith("000")),
    XmlRule("13", extractParentUcrId, endsWith("00")),
    XmlRule("15", extractTransportEquipmentNumberOfSeals, equalsTo("40")),
    XmlRule("18", extractParentUcrId, endsWith("11")),
    XmlRule("35", extractParentUcrId, endsWith("22")),
    XmlRule("39", extractLocationOfGoodsAdditionalIdentifier, equalsTo("98")),
    XmlRule("40", extractLocationOfGoodsAdditionalIdentifier, equalsTo("12")),
    XmlRule("50", extractCustomsOfficeOExitReferenceNumber, endsWith("111")),
    XmlRule("51", extractTransportEquipmentNumberOfSeals, equalsTo("50")),
    XmlRule("52", extractParentUcrId, endsWith("33")),
    XmlRule("53", extractParentUcrId, endsWith("44")),
    XmlRule("54", extractGoodsMeasureGrossMass, equalsTo("2.1")),
    XmlRule("55", extractGoodsMeasureNetMass, equalsTo("88.8")),
    XmlRule("56", extractGoodsMeasureGrossMass, equalsTo("1.1")),
    XmlRule("57", extractGoodsMeasureNetMass, equalsTo("99.9"))
  )
