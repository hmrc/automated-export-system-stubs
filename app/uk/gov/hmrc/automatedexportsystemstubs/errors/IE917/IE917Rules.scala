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

package uk.gov.hmrc.automatedexportsystemstubs.errors.IE917

import uk.gov.hmrc.automatedexportsystemstubs.errors.IE917.XmlRule
import uk.gov.hmrc.automatedexportsystemstubs.utils.AsyncErrorResponseHelper.*
import uk.gov.hmrc.automatedexportsystemstubs.utils.AsyncRequestHelper.*

object IE917Rules:
  val all: List[XmlRule] = List(
    XmlRule("12", "Body.CustomsOfficeOExitActual.referenceNumber", extractCustomsOfficeOExitReferenceNumber, endsWith("000")),
    XmlRule("13", "Body.GoodsShipment.Consignment.parentUCRID", extractParentUcrId, endsWith("00")),
    XmlRule("15", "Body.GoodsShipment.Consignment.TransportEquipment.numberOfSeals", extractTransportEquipmentNumberOfSeals, equalsTo("40")),
    XmlRule("18", "Body.GoodsShipment.Consignment.parentUCRID", extractParentUcrId, endsWith("11")),
    XmlRule("35", "Body.GoodsShipment.Consignment.parentUCRID", extractParentUcrId, endsWith("22")),
    XmlRule("39", "Body.GoodsShipment.Consignment.LocationOfGoods.additionalIdentifier", extractLocationOfGoodsAdditionalIdentifier, equalsTo("98")),
    XmlRule("40", "Body.GoodsShipment.Consignment.LocationOfGoods.additionalIdentifier", extractLocationOfGoodsAdditionalIdentifier, equalsTo("12")),
    XmlRule("50", "GoodsShipment.Consignment.LocationOfGoods.authorisationNumber", extractCustomsOfficeOExitReferenceNumber, endsWith("111")),
    XmlRule("51", "Body.GoodsShipment.Consignment.TransportEquipment.numberOfSeals", extractTransportEquipmentNumberOfSeals, equalsTo("50")),
    XmlRule("52", "Body.GoodsShipment.Consignment.parentUCRID", extractParentUcrId, endsWith("33")),
    XmlRule("53", "Body.GoodsShipment.Consignment.parentUCRID", extractParentUcrId, endsWith("44")),
    XmlRule("54", "Body.GoodsItem.Commodity.GoodsMeasure.grossMass", extractGoodsMeasureGrossMass, equalsTo("2.1")),
    XmlRule("55", "Body.GoodsItem.Commodity.GoodsMeasure.netMass", extractGoodsMeasureNetMass, equalsTo("88.8")),
    XmlRule("56", "Body.GoodsItem.Commodity.GoodsMeasure.grossMass", extractGoodsMeasureGrossMass, equalsTo("1.1")),
    XmlRule("57", "GoodsShipment.Consignment.ReferenceNumberUCRID", extractGoodsMeasureNetMass, equalsTo("99.9"))
  )
