package toph.fixture

import core.KeyGenerator
import testkit.core.createRandomName
import testkit.core.createRandomResourceID
import toph.model.data.EventData

object EventDataFixture:

  def createRandom() = EventData(
    key = createRandomResourceID(),
    preferredOriginKey = Some(KeyGenerator.next32()),
    preferedMagnitudeKey = Some(KeyGenerator.next32()),
    `type` = Some(KeyGenerator.next4()),
    typeUncertainty = Some(KeyGenerator.next4()),
    description = Seq(createRandomName()),
    comment = Seq(createRandomName()),
    creationInfo = Some(CreationInfoDataFixture.createRandom()),
    originKey = Seq(KeyGenerator.next32()),
    magnitudeKey = Seq(KeyGenerator.next32())
  )
