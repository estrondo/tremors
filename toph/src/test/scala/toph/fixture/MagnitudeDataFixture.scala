package toph.fixture

import testkit.core.createRandomResourceID
import testkit.core.createRandomKey
import testkit.core.createRandomName
import toph.model.data.MagnitudeData
import scala.util.Random

object MagnitudeDataFixture:

  def createRandom() = MagnitudeData(
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
    creationInfo = Some(CreationInfoDataFixture.createRandom())
  )
