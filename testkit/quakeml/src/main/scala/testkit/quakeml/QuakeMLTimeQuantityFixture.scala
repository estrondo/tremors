package testkit.quakeml

import quakeml.QuakeMLTimeQuantity
import testkit.core.createZonedDateTime

object QuakeMLTimeQuantityFixture:

  def createRandom() = QuakeMLTimeQuantity(
    value = createZonedDateTime(),
    uncertainty = Some(0)
  )
