package testkit.quakeml

import java.util.UUID
import quakeml.*
import scala.util.Random

def createRandomString(): String =
  val chars =
    for _ <- 0 until 32
    yield Random.nextPrintableChar()

  chars.mkString
