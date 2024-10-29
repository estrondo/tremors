package tremors.quakeml

import scala.util.Random

object IntegerQuantityFixture:

  def createRandom(): IntegerQuantity = IntegerQuantity(
    value = Random.nextInt(100) + 1,
    uncertainty = Some(Random.nextInt(10)),
  )
