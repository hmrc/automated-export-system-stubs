package uk.gov.hmrc.automatedexportsystemstubs.xml

import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.automatedexportsystemstubs.helpers.XmlOps

import scala.xml.{Elem, NodeSeq}

class XmlPathTest extends AnyFreeSpecLike, Matchers:
  "XmlPathNode" - {

    ".apply" - {

      "should query a NodeSeq" - {

        "and return a non-empty NodeSeq" - {

          "XmlKeyPathNode" in {
            val pathNode: XmlPathNode = XmlKeyPathNode("key")

            val xml: Elem =
              <xml>
                <key>value</key>
              </xml>

            XmlOps.normalize(pathNode(xml)) shouldBe XmlOps.normalize(<key>value</key>)
          }

          "XmlIdxPathNode" in {
            val pathNode: XmlIdxPathNode = XmlIdxPathNode(0)

            val xml: Elem =
              <xml>
                <key>value</key>
              </xml>

            XmlOps.normalize(pathNode(xml)) shouldBe XmlOps.normalize(xml)
          }
        }

        "and return an empty NodeSeq" - {

          "XmlKeyPathNode" - {

            "when there is no child with the specified key" in {
              val pathNode: XmlPathNode = XmlKeyPathNode("otherKey")

              val xml: Elem =
                <xml>
                  <key>value</key>
                </xml>

              pathNode(xml) shouldBe NodeSeq.Empty
            }
          }

          "XmlIdxPathNode" - {

            "when there is no element at that index" in {
              val pathNode: XmlIdxPathNode = XmlIdxPathNode(1)

              val xml: Elem =
                <xml>
                  <key>value</key>
                </xml>

              pathNode(xml) shouldBe NodeSeq.Empty
            }
          }

        }
      }
    }

    ".toString" - {

      "should return the string representation of the path node" - {

        "XmlKeyPathNode" in {
          val pathNode: XmlPathNode = XmlKeyPathNode("key")

          pathNode.toString shouldBe "/key"
        }

        "XmlIdxPathNode" in {
          val pathNode: XmlPathNode = XmlIdxPathNode(10)

          pathNode.toString shouldBe "[10]"
        }
      }
    }
  }

  "XmlPath" - {

    ".\\" - {

      "should add an XmlKeyPathNode to the path list" in {
        val path: XmlPath = XmlPath

        (path \ "key") shouldBe XmlPath(List(XmlKeyPathNode("key")))
      }
    }

    ".apply" - {

      "should add an XmlIdxKeyPathNode to the path list" in {
        val path: XmlPath = XmlPath

        path(1) shouldBe XmlPath(List(XmlIdxPathNode(1)))
      }
    }

    ".toString" - {

      "should return the string representation of the path" in {
        val path: XmlPath = (XmlPath \ "key1")(2) \ "key2"

        path.toString shouldBe "/key1[2]/key2"
      }
    }
  }
