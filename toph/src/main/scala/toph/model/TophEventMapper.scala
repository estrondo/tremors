package toph.model

import org.locationtech.jts.geom.CoordinateXY
import org.locationtech.jts.geom.GeometryFactory
import scala.collection.immutable.HashMap
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.Event
import zio.RIO
import zio.ZIO

object TophEventMapper:

  def apply(event: Event): RIO[KeyGenerator & GeometryFactory, Seq[TophEvent]] =
    for
      keyGenerator    <- ZIO.service[KeyGenerator]
      geometryFactory <- ZIO.service[GeometryFactory]
    yield
      val getOrigin            = HashMap.from(event.origin.map(o => (o.publicId, o))).get(_)
      val preferredOriginId    = event.preferredOriginId.map(_.resourceId)
      val preferredMagnitudeId = event.preferredMagnitudeId.map(_.resourceId)

      for
        magnitude <- event.magnitude
        origin    <- magnitude.originId.flatMap(getOrigin)
      yield TophEvent(
        id = keyGenerator.generate(KeyLength.Medium),
        eventId = event.publicId.resourceId,
        preferredOriginId = preferredOriginId,
        preferredMagnitude = preferredMagnitudeId,
        originId = origin.publicId.resourceId,
        originTime = origin.time.value,
        originLocation = geometryFactory.createPoint(CoordinateXY(origin.longitude.value, origin.latitude.value)),
        originUncertainty = Seq(origin.longitude.uncertainty.getOrElse(0), origin.latitude.uncertainty.getOrElse(0)),
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
