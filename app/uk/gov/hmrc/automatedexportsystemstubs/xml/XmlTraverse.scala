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

import scala.xml.{Node, NodeSeq}

final case class XmlTraversedValue[+T](value: T, path: XmlPath)

object XmlTraverse:
  def findAll[T](xml: NodeSeq, path: String, p: Node => T): Seq[XmlTraversedValue[T]] =
    val queries: List[String] = path.split('/').map(_.trim).filter(_.nonEmpty).toList

    def find(node: Node, currentPath: XmlPath, remainingQueries: List[String]): Seq[XmlTraversedValue[T]] =
      remainingQueries match
        case Nil =>
          List(XmlTraversedValue(p(node), currentPath))
        case query :: remaining =>
          val children: NodeSeq = node \ query
          val path:     XmlPath = XmlPath \ query

          children.zipWithIndex.flatMap { case (child, idx) =>
            val childPath: XmlPath =
              if children.length > 1 then path(idx) else path

            find(child, XmlPath(currentPath.path ++ childPath.path), remaining)
          }

    xml.flatMap(n => find(n, XmlPath \ n.label, queries))
  end findAll
