package quakeml

import scala.util.Random

object MagnitudeFixture:

  def createRandom() = Magnitude(
    publicID = ResourceReferenceFixture.createRandom(),
    mag = RealQuantityFixture.createRandom(),
    `type` = Some("undefined"),
    originID = Some(ResourceReferenceFixture.createRandom()),
    methodID = Some(ResourceReferenceFixture.createRandom()),
    stationCount = Some(1 + Random.nextInt(5)),
    azimuthalGap = Some(Random.nextDouble()),
    evaluationMode = Some(EvaluationModeFixture.createRandom()),
    evaluationStatus = Some(EvaluationStatusFixture.createRandom()),
    comment = Seq(CommentFixture.createRandom()),
    creationInfo = Some(CreationInfoFixture.createRandom())
  )
