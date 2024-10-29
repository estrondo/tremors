package tremors.quakeml

object CompositeTimeFixture:

  def createRandom() = CompositeTime(
    year = Some(IntegerQuantityFixture.createRandom()),
    month = Some(IntegerQuantityFixture.createRandom()),
    day = Some(IntegerQuantityFixture.createRandom()),
    hour = Some(IntegerQuantityFixture.createRandom()),
    minute = Some(IntegerQuantityFixture.createRandom()),
    second = Some(IntegerQuantityFixture.createRandom()),
  )
