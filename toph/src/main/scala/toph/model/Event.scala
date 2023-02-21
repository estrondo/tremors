package toph.model

import org.locationtech.jts.geom.Point
import toph.model.data.CreationInfoData

import java.time.ZonedDateTime

case class Event(
    key: String,
    eventKey: String,
    hypocentreKey: Option[String],
    magnitudeKey: Option[String],
    eventType: Option[String],
    position: Option[Point],
    positionUncertainty: Option[Uncertainty2D],
    depth: Option[Double],
    depthUncertainty: Option[Double],
    time: Option[ZonedDateTime],
    timeUncertainty: Option[Int],
    stationCount: Option[Int],
    magnitude: Option[Double],
    magnitudeType: Option[String],
    creationInfo: Option[CreationInfoData]
)
