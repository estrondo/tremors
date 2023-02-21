package testkit.quakeml

import quakeml.QuakeMLMagnitude

import scala.util.Random

object QuakeMLMagnitudeFixture:

  def createRandom() = QuakeMLMagnitude(
    publicID = QuakeMLResourceReferenceFixture.createRandom(),
    mag = QuakeMLRealQuantityFixture.createRandom(),
    `type` = Some("undefined"),
    originID = Some(QuakeMLResourceReferenceFixture.createRandom()),
    methodID = Some(QuakeMLResourceReferenceFixture.createRandom()),
    stationCount = Some(1 + Random.nextInt(5)),
    azimuthalGap = Some(Random.nextDouble()),
    evaluationMode = Some(QuakeMLEvaluationModeFixture.createRandom()),
    evaluationStatus = Some(QuakeMLEvaluationStatusFixture.createRandom()),
    comment = Seq(QuakeMLCommentFixture.createRandom()),
    creationInfo = Some(QuakeMLCreationInfoFixture.createRandom())
  )
