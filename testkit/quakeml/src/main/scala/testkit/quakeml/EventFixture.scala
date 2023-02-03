package testkit.quakeml

import quakeml.*

object EventFixture:

  def createRandom() = Event(
    publicID = ResourceReferenceFixture.createRandom(),
    preferredOriginID = None,
    preferredMagnitudeID = None,
    `type` = Some(Event.Type(createRandomString())),
    typeCertainty = Some(Event.TypeCertainty("woohoo")),
    description = Seq(createRandomDescription()),
    comment = Seq(CommentFixture.createRandom()),
    creationInfo = Some(CreationInfoFixture.createRandom()),
    origin = Seq(OriginFixture.createRandom()),
    magnitude = Seq(MagnitudeFixture.createRandom())
  )

  def createRandomDescription() = Event.Description(
    text = createRandomString(),
    `type` = Event.DescriptionType(createRandomString())
  )
