package toph.model

import java.time.ZonedDateTime

case class Hypocentre(
    key: String,
    position: Point3D,
    positionUncertainty: Uncertainty3D,
    time: ZonedDateTime,
    timeUncertainty: Int
)
