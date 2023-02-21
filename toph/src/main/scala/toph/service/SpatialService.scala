package toph.service

import grpc.toph.spatial.ZioSpatial.ZSpatialService
import grpc.toph.spatial.{Event => GRPCEvent}
import grpc.toph.spatial.{EventQuery => GRPCEventQuery}
import io.grpc.Status
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.ServerMain
import toph.manager.SpatialManager
import zio.ZIO
import zio.stream.Stream
import zio.stream.ZStream
import toph.converter.EventQueryConverter
import toph.converter.GRPCEventConverter
import zio.IO

object SpatialService:

  def apply(manager: SpatialManager): ZSpatialService[RequestContext] = Impl(manager)

  private class Impl(manager: SpatialManager) extends ZSpatialService[RequestContext]:

    override def searchEvent(request: GRPCEventQuery, context: RequestContext): Stream[Status, GRPCEvent] =

      def search(any: Any): IO[Status, Stream[Status, GRPCEvent]] =
        for query <- EventQueryConverter
                       .from(request)
                       .tapErrorCause(ZIO.logErrorCause("It was impossible to read the EvenQuery!", _))
                       .mapError(_ => Status.INVALID_ARGUMENT)
        yield manager
          .search(query)
          .mapZIO(GRPCEventConverter.from)
          .tapErrorCause(ZIO.logErrorCause("It was impossible to search events!", _))
          .mapError(_ => Status.INTERNAL)

      ZStream
        .logDebug("Searching for events.")
        .mapZIO(search)
        .flatten
