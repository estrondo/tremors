package toph.converter

import ducktape.jts.given
import grpc.toph.spatial.{Event => GRPCEvent}
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import scalapb.UnknownFieldSet
import toph.model.Event
import zio.Task
import zio.ZIO

object GRPCEventConverter:

  def from(input: Event): Task[GRPCEvent] = ZIO.attempt {
    input
      .into[GRPCEvent]
      .transform(
        Field.const(_.unknownFields, UnknownFieldSet.empty)
      )
  }
