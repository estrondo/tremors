package quakeml

import java.time.ZonedDateTime

case class TimeQuantity(
    value: ZonedDateTime,
    uncertainty: Option[Int]
)

case class RealQuantity(
    value: Double,
    uncertainty: Option[Double]
)
