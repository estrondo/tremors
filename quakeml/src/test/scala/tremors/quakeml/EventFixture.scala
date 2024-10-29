package tremors.quakeml

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object EventFixture:

  def createRandom(): Event = Event(
    publicId = ResourceReferenceFixture.createRandom(),
    preferredOriginId = Some(ResourceReferenceFixture.createRandom()),
    preferredMagnitudeId = Some(ResourceReferenceFixture.createRandom()),
    preferredFocalMechanismId = Some(ResourceReferenceFixture.createRandom()),
    `type` = Some(KeyGenerator.generate(KeyLength.Medium)),
    typeUncertainty = Some(KeyGenerator.generate(KeyLength.Short)),
    description = Seq(EventDescriptionFixture.createRandom()),
    comment = Seq(CommentFixture.createRandom()),
    creationInfo = Some(CreationInfoFixture.createRandom()),
    origin = Seq(OriginFixture.createRandom()),
    magnitude = Seq(MagnitudeFixture.createRandom()),
  )
