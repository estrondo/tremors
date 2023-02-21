package webapi.converter

import grpc.toph.spatial.{Event => TophGRPCEvent}
import grpc.webapi.spatial.{Event => GRPCEvent}
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import zio.Task
import zio.ZIO

object GRPCEventConverter:

  def from(event: TophGRPCEvent): Task[GRPCEvent] = ZIO.attempt {
    event
      .into[GRPCEvent]
      .transform()
  }
