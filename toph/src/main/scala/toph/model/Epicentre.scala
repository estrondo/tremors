package toph.model

import org.locationtech.jts.geom.Point

import java.time.ZonedDateTime

case class Epicentre(
    key: String,
    position: Point,
    positionUncertainty: Uncertainty2D,
    time: ZonedDateTime,
    timeUncertainty: Int
)
