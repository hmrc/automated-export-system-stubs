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

package uk.gov.hmrc.automatedexportsystemstubs.errors.IE906

import uk.gov.hmrc.automatedexportsystemstubs.utils.AsyncErrorResponseHelper.*
import uk.gov.hmrc.automatedexportsystemstubs.utils.AsyncRequestHelper.*

object IE906Rules:
  val all: List[FunctionalRule] = List(
    FunctionalRule("8", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("000")),

    FunctionalRule("12", "GoodsShipment.Consignment.LocationOfGoods.typeOfLocation", extractTypeOfLocation, equalsTo("E")),

    FunctionalRule("13", "GoodsShipment.Consignment.LocationOfGoods.authorisationNumber", extractAuthorisationNumber, equalsTo("1104")),
    FunctionalRule("14", "GoodsShipment.Consignment.LocationOfGoods.authorisationNumber", extractAuthorisationNumber, equalsTo("1103")),
    FunctionalRule("15", "GoodsShipment.Consignment.LocationOfGoods.authorisationNumber", extractAuthorisationNumber, equalsTo("1100")),
    FunctionalRule("26", "GoodsShipment.Consignment.LocationOfGoods.authorisationNumber", extractAuthorisationNumber, equalsTo("1101")),
    FunctionalRule("27", "GoodsShipment.Consignment.LocationOfGoods.authorisationNumber", extractAuthorisationNumber, equalsTo("1102")),

    FunctionalRule("35", "GoodsShipment.Consignment.parentUCRID", extractParentUcrId, endsWith("A1")),

    FunctionalRule("50", "ExportOperation.MRN", extractMrn, endsWith("B3")),
    FunctionalRule("51", "ExportOperation.MRN", extractMrn, endsWith("B2")),
    FunctionalRule("52", "ExportOperation.MRN", extractMrn, endsWith("B1")),
    FunctionalRule("90", "ExportOperation.MRN", extractMrn, endsWith("B0")),

    FunctionalRule("92", "GoodsShipment.Consignment.TransportEquipment.sequenceNumber", extractTransportEquipmentSequenceNumber, equalsTo("40")),
    FunctionalRule("92", "GoodsShipment.Consignment.TransportEquipment.Seal.sequenceNumber", extractSealSequenceNumber, equalsTo("40")),
    FunctionalRule(
      "92",
      "GoodsShipment.Consignment.TransportEquipment.GoodsReference.sequenceNumber",
      extractGoodsReferenceSequenceNumber,
      equalsTo("40")
    ),
    FunctionalRule("92", "GoodsShipment.Consignment.LocationOfGoods.sequenceNumber", extractLocationOfGoodsSequenceNumber, equalsTo("40")),
    FunctionalRule("92", "GoodsShipment.Consignment.TransportDocument.sequenceNumber", extractTransportDocumentSequenceNumber, equalsTo("40")),
    FunctionalRule("92", "GoodsShipment.GoodsItem.Commodity.Packaging.sequenceNumber", extractPackagingSequenceNumber, equalsTo("40")),

    FunctionalRule("93", "ExportOperation.MRN", extractMrn, endsWith("A4")),
    FunctionalRule("94", "ExportOperation.MRN", extractMrn, endsWith("B4")),
    FunctionalRule("95", "ExportOperation.MRN", extractMrn, endsWith("B5")),

    FunctionalRule("96", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("001")),
    FunctionalRule("97", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("002")),
    FunctionalRule("98", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("003")),
    FunctionalRule("99", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("004"))
  )
