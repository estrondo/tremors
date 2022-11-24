package testkit.quakeml

import quakeml.*

object ResourceReferenceFixture:

  def createRandom() = ResourceReference(s"http://test/${createRandomString()}")
