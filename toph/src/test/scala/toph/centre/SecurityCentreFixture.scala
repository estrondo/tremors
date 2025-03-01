package toph.centre

import scala.util.Random
import tremors.random

object SecurityCentreFixture:

  def createRandomContext(): SecurityCentre.Context = SecurityCentre.Context(
    device = s"tremors:${Array("android", "iphone").toIndexedSeq.random}:${1 + Random.nextInt(19)}",
    origin = Array(Some("127.0.0.1"), None, Some("localhost")).toIndexedSeq.random,
  )
