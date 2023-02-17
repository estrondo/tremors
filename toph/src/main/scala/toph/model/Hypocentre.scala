package toph.model

import org.locationtech.jts.geom.Point

import java.time.ZonedDateTime

case class Hypocentre(
    key: String,
    position: Point,
    depth: Double,
    positionUncertainty: Uncertainty3D,
    time: ZonedDateTime,
    timeUncertainty: Int
)
