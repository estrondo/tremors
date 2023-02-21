package testkit.quakeml

import quakeml.QuakeMLEvent
import quakeml.*
object QuakeMLEventFixture:

  def createRandom() = QuakeMLEvent(
    publicID = QuakeMLResourceReferenceFixture.createRandom(),
    preferredOriginID = None,
    preferredMagnitudeID = None,
    `type` = Some(QuakeMLEvent.Type(createRandomString())),
    typeCertainty = Some(QuakeMLEvent.TypeCertainty("woohoo")),
    description = Seq(createRandomDescription()),
    comment = Seq(QuakeMLCommentFixture.createRandom()),
    creationInfo = Some(QuakeMLCreationInfoFixture.createRandom()),
    origin = Seq(QuakeMLOriginFixture.createRandom()),
    magnitude = Seq(QuakeMLMagnitudeFixture.createRandom())
  )

  def createRandomDescription() = QuakeMLEvent.Description(
    text = createRandomString(),
    `type` = QuakeMLEvent.DescriptionType(createRandomString())
  )
