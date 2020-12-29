package uml

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class PumlSpec extends AnyWordSpec {
  private val umlString =
    s"""@startuml
       |Bob -> Alice : hello
       |@enduml""".stripMargin
  private val encoded =
    "SoWkIImgAStDuNBAJrBGjLDmpCbCJbMmKiX8pSd9vt98pKi1IW80"

  "Puml.decode" should {
    "return expected string" in {

      Puml.decode("", encoded).text shouldBe umlString
    }
  }
}
