package toph.converter

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import org.locationtech.jts.geom.CoordinateSequence
import toph.grpc.{EventQuery => GRPCEventQuery}
import toph.query.EventQuery
import zio.Task
import zio.ZIO

object EventQueryConverter:

  def from(input: GRPCEventQuery): Task[EventQuery] = ZIO.attempt {
    input
      .into[EventQuery]
      .transform(
        Field.const(_.boundary, Option.when(input.boundary.nonEmpty)(input.boundary): Option[CoordinateSequence]),
        Field.const(_.magnitudeType, Option.when(input.magnitudeType.nonEmpty)(input.magnitudeType.toSet))
      )
  }
