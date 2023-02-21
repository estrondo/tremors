package testkit.quakeml

import quakeml.QuakeMLOrigin
import testkit.core.createRandomKey

object QuakeMLOriginFixture:

  def createRandom() = QuakeMLOrigin(
    publicID = QuakeMLResourceReferenceFixture.createRandom(),
    time = QuakeMLTimeQuantityFixture.createRandom(),
    longitude = QuakeMLRealQuantityFixture.createRandom(),
    latitude = QuakeMLRealQuantityFixture.createRandom(),
    depth = Some(QuakeMLRealQuantityFixture.createRandom()),
    depthType = Some(createRandomDepthType()),
    referenceSystemID = Some(QuakeMLResourceReferenceFixture.createRandom()),
    methodID = Some(QuakeMLResourceReferenceFixture.createRandom()),
    earthModelID = Some(QuakeMLResourceReferenceFixture.createRandom()),
    `type` = Some(createRandomType()),
    region = Some(createRandomKey(16)),
    evaluationMode = Some(QuakeMLEvaluationModeFixture.createRandom()),
    evaluationStatus = Some(QuakeMLEvaluationStatusFixture.createRandom()),
    comment = Seq(QuakeMLCommentFixture.createRandom()),
    creationInfo = Some(QuakeMLCreationInfoFixture.createRandom())
  )

  def createRandomDepthType() = QuakeMLOrigin.DepthType(
    value = createRandomKey(8)
  )

  def createRandomType() = QuakeMLOrigin.Type(
    value = createRandomKey(8)
  )
