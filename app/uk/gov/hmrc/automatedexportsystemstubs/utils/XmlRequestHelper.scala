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

import scala.xml.{Elem, NodeSeq}

object XmlRequestHelper:

  def endsWith(suffix: String): String => Boolean =
    _.trim.endsWith(suffix)

  def equalsTo(v: String): String => Boolean =
    _.trim == v

  def textAt(ns: NodeSeq): Option[String] =
    ns.headOption.map(_.text).map(_.trim).filter(_.nonEmpty)

  def extract(path: String)(xml: Elem): Option[String] =
    val names = path.split('.').toList
    val nodes = names.foldLeft(xml: NodeSeq)((acc, name) => acc \ name)
    textAt(nodes)
