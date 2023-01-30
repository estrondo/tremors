package testkit.quakeml

import quakeml.TimeQuantity
import testkit.core.createZonedDateTime

object TimeQuantityFixture:

  def createRandom() = TimeQuantity(
    value = createZonedDateTime(),
    uncertainty = Some(0.1)
  )
