package toph.fixture

import toph.model.Magnitude
import testkit.core.createRandomResourceID
import testkit.core.createRandomKey
import testkit.core.createRandomName
import scala.util.Random

object MagnitudeFixture:

  def createRandom() = Magnitude(
    key = createRandomResourceID(),
    mag = Random.between(0, 8),
    `type` = Some(createRandomKey()),
    originID = Some(createRandomKey()),
    methodID = Some(createRandomKey()),
    stationCount = Some(Random.between(0, 5)),
    azimuthalGap = Some(Random.between(0, 10)),
    evaluationMode = Some(createRandomKey()),
    evaluationStatus = Some(createRandomKey()),
    comment = Seq(createRandomName()),
    creationInfo = Some(CreationInfoFixture.createRandom())
  )
