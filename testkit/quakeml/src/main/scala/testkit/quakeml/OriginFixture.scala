package testkit.quakeml

import quakeml.Origin
import testkit.core.createRandomKey

object OriginFixture:

  def createRandom() = Origin(
    publicID = ResourceReferenceFixture.createRandom(),
    time = TimeQuantityFixture.createRandom(),
    longitude = RealQuantityFixture.createRandom(),
    latitude = RealQuantityFixture.createRandom(),
    depth = Some(RealQuantityFixture.createRandom()),
    depthType = Some(createRandomDepthType()),
    referenceSystemID = Some(ResourceReferenceFixture.createRandom()),
    methodID = Some(ResourceReferenceFixture.createRandom()),
    earthModelID = Some(ResourceReferenceFixture.createRandom()),
    `type` = Some(createRandomType()),
    region = Some(createRandomKey(16)),
    evaluationMode = Some(EvaluationModeFixture.createRandom()),
    evaluationStatus = Some(EvaluationStatusFixture.createRandom()),
    comment = Seq(CommentFixture.createRandom()),
    creationInfo = Some(CreationInfoFixture.createRandom())
  )

  def createRandomDepthType() = Origin.DepthType(
    value = createRandomKey(8)
  )

  def createRandomType() = Origin.Type(
    value = createRandomKey(8)
  )
