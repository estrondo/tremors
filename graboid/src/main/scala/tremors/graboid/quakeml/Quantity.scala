package tremors.graboid.quakeml

import java.time.ZonedDateTime

case class TimeQuantity(
    value: ZonedDateTime,
    uncertainty: Option[Double]
)

case class RealQuantity(
    value: Double,
    uncertainty: Option[Double]
)
