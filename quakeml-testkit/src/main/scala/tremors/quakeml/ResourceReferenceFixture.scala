package tremors.quakeml

object ResourceReferenceFixture:

  def createRandom() = ResourceReference(s"http://test/${createRandomString()}")
