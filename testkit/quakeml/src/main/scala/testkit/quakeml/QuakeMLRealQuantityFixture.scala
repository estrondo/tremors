package testkit.quakeml

import quakeml.*
import scala.util.Random

object QuakeMLRealQuantityFixture:

  def createRandom() = QuakeMLRealQuantity(
    value = Random.nextDouble() * 10,
    uncertainty = Some(1e-6)
  )
