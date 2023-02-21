package quakeml

import java.time.ZonedDateTime

case class QuakeMLTimeQuantity(
    value: ZonedDateTime,
    uncertainty: Option[Double]
)

case class QuakeMLRealQuantity(
    value: Double,
    uncertainty: Option[Double]
)
