package testkit.quakeml

import quakeml.*
import testkit.core.createRandomResourceID

object ResourceReferenceFixture:

  def createRandom() = ResourceReference(createRandomResourceID())
