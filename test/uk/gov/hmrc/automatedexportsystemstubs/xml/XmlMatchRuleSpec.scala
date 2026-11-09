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

package uk.gov.hmrc.automatedexportsystemstubs.xml

import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers

import scala.xml.NodeSeq

class XmlMatchRuleSpec extends AnyFreeSpecLike, Matchers:
  "XmlMatchRule" - {

    ".check" - {

      "should find all matching traversed values at the specified path" in {
        val xml: NodeSeq =
          <xml>
            <field1>
              <field2>
              </field2>
              <field2>
                <field3>
                  <field4>123</field4>
                </field3>
                <field3>
                  <field4>abc</field4>
                  <field4>abcd</field4>
                </field3>
              </field2>
            </field1>
          </xml>

        val xmlMatchRule: XmlMatchRule[String] = new XmlMatchRule[String](
          "field1/field2/field3/field4",
          _.startsWith("abc"),
          _.text.trim
        ) {}

        val matchedTraversedValues: Seq[(String, String)] =
          Seq(
            "abc"  -> "/xml/field1/field2[1]/field3[1]/field4[0]",
            "abcd" -> "/xml/field1/field2[1]/field3[1]/field4[1]"
          )

        val result: Seq[(String, String)] = xmlMatchRule
          .check(xml)
          .map(traversed => (traversed.value, traversed.path.toString))

        result shouldBe matchedTraversedValues
      }
    }
  }
