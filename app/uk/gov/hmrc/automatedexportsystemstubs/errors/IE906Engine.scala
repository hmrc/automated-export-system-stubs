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

import scala.xml.Elem
import IE906Rules.all

object Ie906Engine:
  final case class MatchResult(code: String, pointer: String, originalValue: String)

  def allMatches(xml: Elem): Seq[MatchResult] =
    all.flatMap { rule =>
      rule.valueExtractor(xml).filter(rule.matches).map(v => MatchResult(rule.code, rule.elementPath, v))
    }

  def toFunctionalError(m: MatchResult): Elem =
    <FunctionalError>
      <errorPointer>{m.pointer.trim}</errorPointer>
      <errorCode>{m.code}</errorCode>
      <errorReason>{"ERR02"}</errorReason>
      <originalAttributeValue>{m.originalValue}</originalAttributeValue>
    </FunctionalError>
