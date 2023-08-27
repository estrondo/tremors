package tremors.quakeml

import scala.util.Random
import tremors.ZonedDateTimeFixture

object TimeQuantityFixture:

  def createRandom(): TimeQuantity = TimeQuantity(
    value = ZonedDateTimeFixture.createRandom(),
    uncertainty = Some(Random.nextDouble() * 10d)
  )
