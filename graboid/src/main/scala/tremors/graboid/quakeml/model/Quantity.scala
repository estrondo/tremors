package tremors.graboid.quakeml.model

import java.time.ZonedDateTime

case class TimeQuantity(
    value: ZonedDateTime,
    uncertainty: Option[Double]
)

case class RealQuantity(
    value: Double,
    uncertainty: Option[Double]
)
