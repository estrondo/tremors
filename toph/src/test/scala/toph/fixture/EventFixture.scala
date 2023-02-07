package toph.fixture

import core.KeyGenerator
import testkit.core.createRandomName
import toph.model.Event

object EventFixture:

  def createRandom() = Event(
    key = KeyGenerator.next8(),
    preferredOriginKey = Some(KeyGenerator.next32()),
    preferedMagnitudeKey = Some(KeyGenerator.next32()),
    `type` = Some(KeyGenerator.next4()),
    typeUncertainty = Some(KeyGenerator.next4()),
    description = Seq(createRandomName()),
    comment = Seq(createRandomName()),
    creationInfo = Some(CreationInfoFixture.createRandom()),
    originKey = Seq(KeyGenerator.next32()),
    magnitudeKey = Seq(KeyGenerator.next32())
  )
