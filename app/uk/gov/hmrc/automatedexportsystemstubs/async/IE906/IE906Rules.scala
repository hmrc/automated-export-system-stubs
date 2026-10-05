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

import uk.gov.hmrc.automatedexportsystemstubs.utils.XmlRequestBodyExtractor.*
import uk.gov.hmrc.automatedexportsystemstubs.utils.XmlRequestHelper.*

object IE906Rules:
  val all: List[FunctionalRule] = List(
    FunctionalRule("8", extractReferenceNumberUCRID, endsWith("000")),

    FunctionalRule("12", extractTypeOfLocation, equalsTo("E")),

    FunctionalRule("13", extractAuthorisationNumber, equalsTo("1104")),
    FunctionalRule("14", extractAuthorisationNumber, equalsTo("1103")),
    FunctionalRule("15", extractAuthorisationNumber, equalsTo("1100")),
    FunctionalRule("26", extractAuthorisationNumber, equalsTo("1101")),
    FunctionalRule("27", extractAuthorisationNumber, equalsTo("1102")),

    FunctionalRule("35", extractParentUcrId, endsWith("A1")),

    FunctionalRule("50", extractMrn, endsWith("B3")),
    FunctionalRule("51", extractMrn, endsWith("B2")),
    FunctionalRule("52", extractMrn, endsWith("B1")),
    FunctionalRule("90", extractMrn, endsWith("B0")),

    FunctionalRule("92", extractTransportEquipmentSequenceNumber, equalsTo("40")),
    FunctionalRule("92", extractSealSequenceNumber, equalsTo("40")),
    FunctionalRule("92", extractGoodsReferenceSequenceNumber, equalsTo("40")),
    FunctionalRule("92", extractLocationOfGoodsSequenceNumber, equalsTo("40")),
    FunctionalRule("92", extractTransportDocumentSequenceNumber, equalsTo("40")),
    FunctionalRule("92", extractPackagingSequenceNumber, equalsTo("40")),

    FunctionalRule("93", extractMrn, endsWith("A4")),
    FunctionalRule("94", extractMrn, endsWith("B4")),
    FunctionalRule("95", extractMrn, endsWith("B5")),

    FunctionalRule("96", extractReferenceNumberUCRID, endsWith("001")),
    FunctionalRule("97", extractReferenceNumberUCRID, endsWith("002")),
    FunctionalRule("98", extractReferenceNumberUCRID, endsWith("003")),
    FunctionalRule("99", extractReferenceNumberUCRID, endsWith("004"))
  )
