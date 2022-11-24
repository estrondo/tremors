package testkit.quakeml

import quakeml.*

import scala.util.Random

object RealQuantityFixture:

  def createRandom() = RealQuantity(
    value = Random.nextDouble() * 10,
    uncertainty = Some(1e-6)
  )
