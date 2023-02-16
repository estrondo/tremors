package toph.converter

import io.github.arainko.ducktape.into
import toph.grpc.spatial.EpicentreQuery
import toph.grpc.spatial.HypocentreQuery
import toph.query.spatial.SpatialEpicentreQuery
import toph.query.spatial.SpatialHypocentreQuery
import zio.Task
import zio.ZIO

object SpatialQueryConverter:

  def from(query: EpicentreQuery): Task[SpatialEpicentreQuery] = ZIO.attempt {
    query
      .into[SpatialEpicentreQuery]
      .transform()
  }

  def from(query: HypocentreQuery): Task[SpatialHypocentreQuery] = ZIO.attempt {
    query
      .into[SpatialHypocentreQuery]
      .transform()
  }
