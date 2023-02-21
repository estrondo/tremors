package toph.converter

import grpc.toph.spatial.{EventQuery => GRPCEventQuery}
import toph.query.EventQuery
import zio.Task
import zio.ZIO
import io.github.arainko.ducktape.{into, Field}

object EventQueryConverter:

  def from(input: GRPCEventQuery): Task[EventQuery] = ZIO.attempt {
    input
      .into[EventQuery]
      .transform()
  }
