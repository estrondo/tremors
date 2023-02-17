package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import scalapb.UnknownFieldSet
import toph.grpc.spatial.GRPCEpicentre
import toph.model.Epicentre
import zio.Task
import zio.ZIO

object GRPCEpicentreConverter:

  def from(input: Epicentre): Task[GRPCEpicentre] = ZIO.attempt {
    input
      .into[GRPCEpicentre]
      .transform(
        Field.const(_.lat, input.position.getY()),
        Field.const(_.lng, input.position.getX()),
        Field.const(_.magnitudeType, ""),
        Field.const(_.magnitude, 0d),
        Field.const(_.unknownFields, UnknownFieldSet.empty)
      )
  }
