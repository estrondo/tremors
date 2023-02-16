package toph.query.spatial

import toph.model.Epicentre
import toph.model.Hypocentre

import java.time.ZonedDateTime

sealed trait SpatialQuery[T]

case class SpatialEpicentreQuery(
    boundary: Seq[Double],
    boundaryRadius: Option[Double],
    minMagnitude: Option[Double],
    maxMagnitude: Option[Double],
    startTime: Option[ZonedDateTime],
    endTime: Option[ZonedDateTime]
) extends SpatialQuery[Epicentre]

case class SpatialHypocentreQuery(
    boundary: Seq[Double],
    boundaryRadius: Option[Double],
    minMagnitude: Option[Double],
    maxMagnitude: Option[Double],
    startTime: Option[ZonedDateTime],
    endTime: Option[ZonedDateTime],
    minDepth: Option[Double],
    maxDepth: Option[Double]
) extends SpatialQuery[Hypocentre]
