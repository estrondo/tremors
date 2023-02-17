package toph.query.spatial

import org.locationtech.jts.geom.CoordinateSequence
import toph.model.Epicentre
import toph.model.Hypocentre

import java.time.ZonedDateTime
import org.locationtech.jts.geom.CoordinateSequences

sealed trait SpatialQuery[T]

case class SpatialEpicentreQuery(
    boundary: CoordinateSequence,
    boundaryRadius: Option[Double],
    minMagnitude: Option[Double],
    maxMagnitude: Option[Double],
    startTime: Option[ZonedDateTime],
    endTime: Option[ZonedDateTime]
) extends SpatialQuery[Epicentre]:

  override def equals(x: Any): Boolean = x match
    case other: SpatialEpicentreQuery =>
      boundaryRadius == other.boundaryRadius &&
      minMagnitude == other.minMagnitude &&
      maxMagnitude == other.maxMagnitude &&
      startTime == other.startTime &&
      endTime == other.endTime && CoordinateSequences.isEqual(boundary, other.boundary)

    case _ => false

case class SpatialHypocentreQuery(
    boundary: CoordinateSequence,
    boundaryRadius: Option[Double],
    minMagnitude: Option[Double],
    maxMagnitude: Option[Double],
    startTime: Option[ZonedDateTime],
    endTime: Option[ZonedDateTime],
    minDepth: Option[Double],
    maxDepth: Option[Double]
) extends SpatialQuery[Hypocentre]:

  override def equals(x: Any): Boolean = x match
    case other: SpatialHypocentreQuery =>
      boundaryRadius == other.boundaryRadius &&
      minMagnitude == other.minMagnitude &&
      maxMagnitude == other.maxMagnitude &&
      startTime == other.startTime &&
      endTime == other.endTime &&
      minDepth == other.minDepth &&
      maxDepth == other.maxDepth && CoordinateSequences.isEqual(boundary, other.boundary)

    case _ => false
