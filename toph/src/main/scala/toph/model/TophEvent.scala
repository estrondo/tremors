package toph.model

import java.time.ZonedDateTime
import org.locationtech.jts.geom.Point

case class TophEvent(
    id: String,
    eventId: String,
    preferredOriginId: Option[String],
    preferredMagnitude: Option[String],
    originId: String,
    originTime: ZonedDateTime,
    originLocation: Point,
    originUncertainty: Seq[Double],
    originDepth: Option[Double],
    originDepthUncertainty: Option[Double],
    originReferenceSystemId: Option[String],
    originMethodId: Option[String],
    originEarthModelId: Option[String],
    magnitudeId: String,
    magnitudeValue: Double,
    magnitudeUncertainty: Option[Double],
    magnitudeOriginId: Option[String],
    magnitudeMethodId: Option[String],
    magnitudeStationCount: Option[Int],
    magnitudeEvaluationMode: Option[String],
    magnitudeEvaluationStatus: Option[String]
)
