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

import scala.xml.NodeSeq

final case class XmlRequestBodyExtractor(path: String):
  private def extractor: NodeSeq => Option[String] =
    XmlRequestHelper.extract(path.trim)

  def extract(xml: NodeSeq): Option[String] =
    extractor(xml)

object XmlRequestBodyExtractor:
  lazy val extractMrn: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.ExportOperation.MRN")

  val extractReferenceNumberUCRID: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.ReferenceNumberUCRID")

  val extractAuthorisationNumber: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.LocationOfGoods.authorisationNumber")

  val extractParentUcrId: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.parentUCRID")

  val extractTypeOfLocation: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.LocationOfGoods.typeOfLocation")

  val extractTransportEquipmentSequenceNumber: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.TransportEquipment.sequenceNumber")

  val extractSealSequenceNumber: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.TransportEquipment.Seal.sequenceNumber")

  val extractGoodsReferenceSequenceNumber: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.TransportEquipment.GoodsReference.sequenceNumber")

  val extractLocationOfGoodsSequenceNumber: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.LocationOfGoods.sequenceNumber")

  val extractTransportDocumentSequenceNumber: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.TransportDocument.sequenceNumber")

  val extractPackagingSequenceNumber: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.GoodsItem.Commodity.Packaging.sequenceNumber")

  val extractCustomsOfficeOExitReferenceNumber: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.CustomsOfficeOExitActual.referenceNumber")

  val extractTransportEquipmentNumberOfSeals: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.TransportEquipment.numberOfSeals")

  val extractLocationOfGoodsAdditionalIdentifier: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsShipment.Consignment.LocationOfGoods.additionalIdentifier")

  val extractGoodsMeasureGrossMass: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsItem.Commodity.GoodsMeasure.grossMass")

  val extractGoodsMeasureNetMass: XmlRequestBodyExtractor =
    XmlRequestBodyExtractor("Body.GoodsItem.Commodity.GoodsMeasure.netMass")
