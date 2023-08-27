package tremors.quakeml

import scala.util.Random
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object MagnitudeFixture:

  def createRandom() = Magnitude(
    publicId = ResourceReferenceFixture.createRandom(),
    mag = RealQuantityFixture.createRandom(),
    `type` = Some(KeyGenerator.generate(KeyLength.Short)),
    originId = Some(ResourceReferenceFixture.createRandom()),
    methodId = Some(ResourceReferenceFixture.createRandom()),
    stationCount = Some(1 + Random.nextInt(9)),
    azimuthalGap = Some(1d + Random.nextDouble() * 10d),
    evaluationMode = Some(KeyGenerator.generate(KeyLength.Short)),
    evaluationStatus = Some(KeyGenerator.generate(KeyLength.Short)),
    comment = Seq(CommentFixture.createRandom()),
    creationInfo = Option(CreationInfoFixture.createRandom())
  )
