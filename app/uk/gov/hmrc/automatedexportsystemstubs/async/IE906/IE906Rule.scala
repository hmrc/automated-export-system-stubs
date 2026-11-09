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

import uk.gov.hmrc.automatedexportsystemstubs.utils.XmlRequestBodyPaths.*
import uk.gov.hmrc.automatedexportsystemstubs.utils.XmlRequestHelper.{endsWith, equalsTo, textAt}
import uk.gov.hmrc.automatedexportsystemstubs.xml.XmlMatchRule

import scala.xml.Node

final case class IE906Rule[T](
  code:    String,
  path:    String,
  matches: T => Boolean,
  parse:   Node => T
) extends XmlMatchRule(path, matches, parse)

object IE906Rule:
  val all: List[IE906Rule[String]] = List(
    IE906Rule("8", referenceNumberUCR, endsWith("000"), textAt),

    IE906Rule("12", typeOfLocation, equalsTo("E"), textAt),

    IE906Rule("13", authorisationNumber, equalsTo("1104"), textAt),
    IE906Rule("14", authorisationNumber, equalsTo("1103"), textAt),
    IE906Rule("15", authorisationNumber, equalsTo("1100"), textAt),
    IE906Rule("26", authorisationNumber, equalsTo("1101"), textAt),
    IE906Rule("27", authorisationNumber, equalsTo("1102"), textAt),

    IE906Rule("35", parentUcrId, endsWith("A1"), textAt),

    IE906Rule("50", mrn, endsWith("B3"), textAt),
    IE906Rule("51", mrn, endsWith("B2"), textAt),
    IE906Rule("52", mrn, endsWith("B1"), textAt),
    IE906Rule("90", mrn, endsWith("B0"), textAt),

    IE906Rule("92", transportEquipmentSequenceNumber, equalsTo("40"), textAt),
    IE906Rule("92", goodsReferenceSequenceNumber, equalsTo("40"), textAt),
    IE906Rule("92", locationOfGoodsSequenceNumber, equalsTo("40"), textAt),
    IE906Rule("92", transportDocumentSequenceNumber, equalsTo("40"), textAt),
    IE906Rule("92", packagingSequenceNumber, equalsTo("40"), textAt),

    IE906Rule("93", mrn, endsWith("A4"), textAt),
    IE906Rule("94", mrn, endsWith("B4"), textAt),
    IE906Rule("95", mrn, endsWith("B5"), textAt),

    IE906Rule("96", referenceNumberUCR, endsWith("001"), textAt),
    IE906Rule("97", referenceNumberUCR, endsWith("002"), textAt),
    IE906Rule("98", referenceNumberUCR, endsWith("003"), textAt),
    IE906Rule("99", referenceNumberUCR, endsWith("004"), textAt)
  )
