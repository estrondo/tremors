package webapi.converter

import grpc.toph.spatial.{EventQuery => TophGRPCEventQuery}
import grpc.webapi.spatial.{EventQuery => GRPCEventQuery}
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import zio.Task
import zio.ZIO

object TophEventQueryConverter:

  def from(query: GRPCEventQuery): Task[TophGRPCEventQuery] = ZIO.attempt {
    query
      .into[TophGRPCEventQuery]
      .transform()
  }
