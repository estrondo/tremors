package toph.model

import java.time.ZonedDateTime

case class Epicentre(
    key: String,
    position: Point2D,
    positionUncertainty: Uncertainty2D,
    time: ZonedDateTime,
    timeUncertainty: Int
)
