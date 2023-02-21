package testkit.quakeml

import quakeml.*
import testkit.core.createRandomResourceID

object QuakeMLResourceReferenceFixture:

  def createRandom() = QuakeMLResourceReference(createRandomResourceID())
