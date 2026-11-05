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

object XmlRequestBodyPaths:
  val mrn: String = "Body/ExportOperation/MRN"

  val referenceNumberUCRID: String = "Body/GoodsShipment/Consignment/ReferenceNumberUCRID"

  val authorisationNumber: String =
    "Body/GoodsShipment/Consignment/LocationOfGoods/authorisationNumber"

  val parentUcrId: String =
    "Body/GoodsShipment/Consignment/parentUCRID"

  val typeOfLocation: String =
    "Body/GoodsShipment/Consignment/LocationOfGoods/typeOfLocation"

  val transportEquipmentSequenceNumber: String =
    "Body/GoodsShipment/Consignment/TransportEquipment/sequenceNumber"

  val sealSequenceNumber: String =
    "Body/GoodsShipment/Consignment/TransportEquipment/Seal/sequenceNumber"

  val goodsReferenceSequenceNumber: String =
    "Body/GoodsShipment/Consignment/TransportEquipment/GoodsReference/sequenceNumber"

  val locationOfGoodsSequenceNumber: String =
    "Body/GoodsShipment/Consignment/LocationOfGoods/sequenceNumber"

  val transportDocumentSequenceNumber: String =
    "Body/GoodsShipment/Consignment/TransportDocument/sequenceNumber"

  val packagingSequenceNumber: String =
    "Body/GoodsShipment/GoodsItem/Commodity/Packaging/sequenceNumber"

  val customsOfficeOfExitReferenceNumber: String =
    "Body/CustomsOfficeOfExitActual/referenceNumber"

  val transportEquipmentNumberOfSeals: String =
    "Body/GoodsShipment/Consignment/TransportEquipment/numberOfSeals"

  val locationOfGoodsAdditionalIdentifier: String =
    "Body/GoodsShipment/Consignment/LocationOfGoods/additionalIdentifier"

  val goodsMeasureGrossMass: String =
    "Body/GoodsShipment/GoodsItem/Commodity/GoodsMeasure/grossMass"

  val goodsMeasureNetMass: String =
    "Body/GoodsShipment/GoodsItem/Commodity/GoodsMeasure/netMass"
