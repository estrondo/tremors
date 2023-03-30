package webapi.service

import grpc.toph.spatial.ZioSpatial.{SpatialServiceClient => TophSpatialServiceClient}
import grpc.webapi.spatial.{Event => GRPCEvent}
import grpc.webapi.spatial.{EventQuery => GRPCEventQuery}
import grpc.webapi.spatial.Event
import grpc.webapi.spatial.EventQuery
import grpc.webapi.spatial.ZioSpatial.ZSpatialService
import io.grpc.Status
import io.grpc.StatusException
import scalapb.zio_grpc.RequestContext
import webapi.converter.GRPCEventConverter
import webapi.converter.TophEventQueryConverter
import zio.RIO
import zio.ZIO
import zio.stream.Stream
import zio.stream.ZStream

object SpatialService:

  def apply(): RIO[TophSpatialServiceClient, ZSpatialService[RequestContext]] =
    ZIO.serviceWith[TophSpatialServiceClient](Impl(_))

  private class Impl(tophClient: TophSpatialServiceClient) extends ZSpatialService[RequestContext]:

    override def searchEvent(request: GRPCEventQuery, context: RequestContext): Stream[StatusException, GRPCEvent] =
      ZStream
        .fromZIO(
          for
            query <- TophEventQueryConverter
                       .from(request)
                       .tapErrorCause(ZIO.logErrorCause("It was impossible to read request!", _))
                       .mapError(_ => Status.INVALID_ARGUMENT.asException)
            _     <- ZIO.logInfo(s"Searching for events: $query.")
          yield tophClient
            .searchEvent(query)
            .mapZIO(GRPCEventConverter.from)
            .tapErrorCause(ZIO.logErrorCause("It was impossible to search events!", _))
            .mapError(_ => Status.INTERNAL.asException)
        )
        .flatten
