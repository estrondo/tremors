package toph.service

import io.grpc.Status
import io.grpc.StatusException
import scalapb.zio_grpc.RequestContext
import toph.converter.EventQueryConverter
import toph.converter.GRPCEventConverter
import toph.grpc.{Event => GRPCEvent}
import toph.grpc.{EventQuery => GRPCEventQuery}
import toph.grpc.ZioGrpc.ZSpatialService
import toph.manager.SpatialManager
import zio.IO
import zio.ZIO
import zio.stream.Stream
import zio.stream.ZStream

object SpatialService:

  def apply(manager: SpatialManager): ZSpatialService[RequestContext] = Impl(manager)

  private class Impl(manager: SpatialManager) extends ZSpatialService[RequestContext]:

    override def searchEvent(request: GRPCEventQuery, context: RequestContext): Stream[StatusException, GRPCEvent] =

      def search(any: Any): IO[StatusException, Stream[StatusException, GRPCEvent]] =
        for query <- EventQueryConverter
                       .from(request)
                       .tapErrorCause(ZIO.logErrorCause("It was impossible to read the EvenQuery!", _))
                       .mapError(_ => Status.INVALID_ARGUMENT.asException)
        yield manager
          .search(query)
          .mapZIO(GRPCEventConverter.from)
          .tapErrorCause(ZIO.logErrorCause("It was impossible to search events!", _))
          .mapError(_ => Status.INTERNAL.asException)

      ZStream
        .logDebug("Searching for events.")
        .mapZIO(search)
        .flatten
