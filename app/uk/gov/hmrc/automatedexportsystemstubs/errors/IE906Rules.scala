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

import uk.gov.hmrc.automatedexportsystemstubs.utils.AsyncErrorResponseHelper.*

object IE906Rules:
  val all: List[XmlRule] = List(
    XmlRule("8", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("000")),
    XmlRule("27", "GoodsShipment.Consignment.LocationOfGoods.authorisationNumber", extractAuthorisationNumber, equalsTo("1102")),
    XmlRule("35", "GoodsShipment.Consignment.parentUCRID", extractParentUcrId, endsWith("AA")),
    XmlRule("90", "ExportOperation.MRN", extractMrn, endsWith("B0")),
    XmlRule("93", "ExportOperation.MRN", extractMrn, endsWith("B1")),
    XmlRule("96", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("001")),
    XmlRule("97", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("002")),
    XmlRule("98", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("003")),
    XmlRule("99", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractReferenceNumberUCRID, endsWith("004"))
  )
