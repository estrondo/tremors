package toph.model

import org.locationtech.jts.geom.CoordinateXY
import scala.util.Random
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.Event
import tremors.quakeml.Magnitude
import tremors.quakeml.Origin

object TophEventFixture:

  def createRandom(): TophEvent =

    def gShort = KeyGenerator.generate(KeyLength.Short)

    TophEvent(
      id = KeyGenerator.generate(KeyLength.Long),
      eventId = gShort,
      preferredOriginId = Some(gShort),
      preferredMagnitude = Some(gShort),
      originId = gShort,
      originTime = ZonedDateTimeFixture.createRandom(),
      originLocation = geometryFactory
        .createPoint(CoordinateXY(Random.nextDouble() * 360d - 180d, Random.nextDouble() * 180d - 90d)),
      originUncertainty = Seq(Random.nextDouble() * 0.5, Random.nextDouble() * 0.5),
      originDepth = Some(Random.nextDouble() * 1000 + 5000),
      originDepthUncertainty = Some(Random.nextDouble() * 100),
      originReferenceSystemId = Some(KeyGenerator.generate(KeyLength.Medium)),
      originMethodId = Some(KeyGenerator.generate(KeyLength.Medium)),
      originEarthModelId = Some(gShort),
      magnitudeId = gShort,
      magnitudeValue = Random.nextDouble() * 6 + 1,
      magnitudeUncertainty = Some(Random.nextDouble() * 0.1),
      magnitudeOriginId = Some(gShort),
      magnitudeMethodId = Some(gShort),
      magnitudeStationCount = Some(Random.nextInt(10)),
      magnitudeEvaluationMode = Some(gShort),
      magnitudeEvaluationStatus = Some(gShort),
    )

  def createRandom(event: Event, origin: Origin, magnitude: Magnitude): TophEvent = TophEvent(
    id = KeyGenerator.generate(KeyLength.Medium),
    eventId = event.publicId.resourceId,
    preferredOriginId = Some(origin.publicId.resourceId),
    preferredMagnitude = Some(magnitude.publicId.resourceId),
    originId = origin.publicId.resourceId,
    originTime = origin.time.value,
    originLocation = geometryFactory.createPoint(CoordinateXY(origin.longitude.value, origin.latitude.value)),
    originUncertainty = Seq(origin.longitude.uncertainty.getOrElse(0d), origin.latitude.uncertainty.getOrElse(0)),
    originDepth = origin.depth.map(_.value),
    originDepthUncertainty = origin.depth.flatMap(_.uncertainty),
    originReferenceSystemId = origin.referenceSystemId.map(_.resourceId),
    originMethodId = origin.methodId.map(_.resourceId),
    originEarthModelId = origin.earthModelId.map(_.resourceId),
    magnitudeId = magnitude.publicId.resourceId,
    magnitudeValue = magnitude.mag.value,
    magnitudeUncertainty = magnitude.mag.uncertainty,
    magnitudeOriginId = magnitude.originId.map(_.resourceId),
    magnitudeMethodId = magnitude.methodId.map(_.resourceId),
    magnitudeStationCount = magnitude.stationCount,
    magnitudeEvaluationMode = magnitude.evaluationMode,
    magnitudeEvaluationStatus = magnitude.evaluationStatus,
  )
