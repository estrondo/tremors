package toph.query

import java.time.ZonedDateTime
import org.locationtech.jts.geom.CoordinateSequence

case class EventQuery(
    boundary: Option[CoordinateSequence],
    boundaryRadius: Option[Double],
    startTime: Option[ZonedDateTime],
    endTime: Option[ZonedDateTime],
    minDepth: Option[Double],
    maxDepth: Option[Double],
    minMagnitude: Option[Double],
    maxMagnitude: Option[Double],
    magnitudeType: Option[Set[String]]
)
