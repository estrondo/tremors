package toph.fixture

import toph.model.Event
import testkit.core.createRandomKey
import testkit.core.createZonedDateTime
import scala.util.Random

object EventFixture:

  def createRandom() = Event(
    key = createRandomKey(),
    eventKey = createRandomKey(),
    hypocentreKey = Some(createRandomKey()),
    magnitudeKey = Some(createRandomKey()),
    eventType = Some(createRandomKey()),
    position = Some(PointFixture.createRandom()),
    positionUncertainty = Some(Uncertainty2DFixture.createRandom()),
    depth = Some(Random.between(1500, 15000)),
    depthUncertainty = Some(Random.between(500, 1000)),
    time = Some(createZonedDateTime()),
    timeUncertainty = Some(Random.between(10, 20)),
    stationCount = Some(Random.between(2, 12)),
    magnitude = Some(Random.between(0d, 8d)),
    magnitudeType = Some(createRandomKey()),
    creationInfo = Some(CreationInfoDataFixture.createRandom())
  )
