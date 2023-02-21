package toph.query

import org.locationtech.jts.geom.CoordinateSequence

import java.time.ZonedDateTime

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
