package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import scalapb.UnknownFieldSet
import toph.grpc.spatial.GRPCHypocentre
import toph.model.Hypocentre
import zio.Task
import zio.ZIO

object GRPCHypocentreConverter:

  def from(input: Hypocentre): Task[GRPCHypocentre] = ZIO.attempt {
    input
      .into[GRPCHypocentre]
      .transform(
        Field.const(_.lng, input.position.lng),
        Field.const(_.lat, input.position.lat),
        Field.const(_.magnitudeType, ""),
        Field.const(_.magnitude, 0d),
        Field.const(_.depth, input.position.z),
        Field.const(_.unknownFields, UnknownFieldSet.empty)
      )
  }
