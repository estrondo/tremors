package tremors.quakeml

import scala.util.Random

object RealQuantityFixture:

  def createRandom(): RealQuantity = RealQuantity(
    value = Random.nextDouble() * 100d,
    uncertainty = Some(Random.nextDouble() * 10d),
  )
