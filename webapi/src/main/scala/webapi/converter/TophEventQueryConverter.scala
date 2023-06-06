package webapi.converter

import toph.grpc.{EventQuery => TophGRPCEventQuery}
import webapi.grpc.{EventQuery => GRPCEventQuery}
import io.github.arainko.ducktape.into
import zio.Task
import zio.ZIO

object TophEventQueryConverter:

  def from(query: GRPCEventQuery): Task[TophGRPCEventQuery] = ZIO.attempt {
    query
      .into[TophGRPCEventQuery]
      .transform()
  }
