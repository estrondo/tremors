package tremors.graboid

import scala.util.Random

def createRandomString(length: Int = 8): String =
  Random.nextString(length)
