package toph.model.data

import java.time.ZonedDateTime
import org.locationtech.jts.geom.Point
import toph.model.Uncertainty2D

case class HypocentreData(
    key: String,
    position: Point,
    positionUncertainty: Uncertainty2D,
    depth: Option[Double],
    depthUncertainty: Option[Double],
    time: ZonedDateTime,
    timeUncertainty: Int
)
