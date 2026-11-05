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

import uk.gov.hmrc.automatedexportsystemstubs.utils.XmlRequestBodyPaths.*
import uk.gov.hmrc.automatedexportsystemstubs.utils.XmlRequestHelper.{endsWith, equalsTo, textAt}
import uk.gov.hmrc.automatedexportsystemstubs.xml.XmlMatchRule

import scala.xml.Node

final case class IE917Rule[T](
  code:    String,
  path:    String,
  matches: T => Boolean,
  parse:   Node => T
) extends XmlMatchRule(path, matches, parse)

object IE917Rule:
  val all: List[IE917Rule[String]] = List(
    IE917Rule("12", customsOfficeOfExitReferenceNumber, endsWith("000"), textAt),
    IE917Rule("13", parentUcrId, endsWith("00"), textAt),
    IE917Rule("15", transportEquipmentNumberOfSeals, equalsTo("40"), textAt),
    IE917Rule("18", parentUcrId, endsWith("11"), textAt),
    IE917Rule("35", parentUcrId, endsWith("22"), textAt),
    IE917Rule("39", locationOfGoodsAdditionalIdentifier, equalsTo("98"), textAt),
    IE917Rule("40", locationOfGoodsAdditionalIdentifier, equalsTo("12"), textAt),
    IE917Rule("50", customsOfficeOfExitReferenceNumber, endsWith("111"), textAt),
    IE917Rule("51", transportEquipmentNumberOfSeals, equalsTo("50"), textAt),
    IE917Rule("52", parentUcrId, endsWith("33"), textAt),
    IE917Rule("53", parentUcrId, endsWith("44"), textAt),
    IE917Rule("54", goodsMeasureGrossMass, equalsTo("2.1"), textAt),
    IE917Rule("55", goodsMeasureNetMass, equalsTo("88.8"), textAt),
    IE917Rule("56", goodsMeasureGrossMass, equalsTo("1.1"), textAt),
    IE917Rule("57", goodsMeasureNetMass, equalsTo("99.9"), textAt)
  )
