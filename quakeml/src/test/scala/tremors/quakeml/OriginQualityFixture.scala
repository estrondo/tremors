package tremors.quakeml

import scala.util.Random
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object OriginQualityFixture:

  def createRandom() = OriginQuality(
    associatedPhaseCount = Some(Random.nextInt(99) + 1),
    usedPhaseCount = Some(Random.nextInt(99) + 1),
    associatedStationCount = Some(Random.nextInt(99) + 1),
    usedStationCount = Some(Random.nextInt(99) + 1),
    depthPhaseCount = Some(Random.nextInt(99) + 1),
    standardError = Some(Random.nextInt(99) + 1),
    azimuthalGap = Some(Random.nextInt(99) + 1),
    secondaryAzimuthalGap = Some(Random.nextInt(99) + 1),
    groundTruthLevel = Some(KeyGenerator.generate(KeyLength.Short)),
    minimumDistance = Some(Random.nextInt(99) + 1),
    maximumDistance = Some(Random.nextInt(99) + 1),
    medianDistance = Some(Random.nextInt(99) + 1)
  )
