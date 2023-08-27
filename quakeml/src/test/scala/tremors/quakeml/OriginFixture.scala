package tremors.quakeml

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object OriginFixture:

  def createRandom(): Origin = Origin(
    publicId = ResourceReferenceFixture.createRandom(),
    time = TimeQuantityFixture.createRandom(),
    longitude = RealQuantityFixture.createRandom(),
    latitude = RealQuantityFixture.createRandom(),
    depth = Some(RealQuantityFixture.createRandom()),
    depthType = Some(KeyGenerator.generate(KeyLength.Short)),
    timeFixed = Some(false),
    epicenterFixed = Some(false),
    referenceSystemId = Some(ResourceReferenceFixture.createRandom()),
    methodId = Some(ResourceReferenceFixture.createRandom()),
    earthModelId = Some(ResourceReferenceFixture.createRandom()),
    compositeTime = Some(CompositeTimeFixture.createRandom()),
    quality = Some(OriginQualityFixture.createRandom()),
    `type` = Some(KeyGenerator.generate(KeyLength.Short)),
    region = Some(KeyGenerator.generate(KeyLength.Short)),
    evaluationMode = Some(KeyGenerator.generate(KeyLength.Short)),
    evaluationStatus = Some(KeyGenerator.generate(KeyLength.Short)),
    comment = Seq(CommentFixture.createRandom()),
    creationInfo = Some(CreationInfoFixture.createRandom())
  )
