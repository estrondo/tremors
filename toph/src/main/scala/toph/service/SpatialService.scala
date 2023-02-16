package toph.service

import io.grpc.Status
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.ServerMain
import toph.converter.GRPCEpicentreConverter
import toph.converter.GRPCHypocentreConverter
import toph.converter.SpatialQueryConverter
import toph.grpc.spatial.EpicentreQuery
import toph.grpc.spatial.GRPCEpicentre
import toph.grpc.spatial.GRPCHypocentre
import toph.grpc.spatial.HypocentreQuery
import toph.grpc.spatial.ZioSpatial.ZSpatialService
import toph.manager.SpatialManager
import zio.ZIO
import zio.stream.Stream
import zio.stream.ZStream

object SpatialService:

  def apply(manager: SpatialManager): ZSpatialService[RequestContext] = Impl(manager)

  private class Impl(manager: SpatialManager) extends ZSpatialService[RequestContext]:

    override def searchEpicentre(request: EpicentreQuery, context: RequestContext): Stream[Status, GRPCEpicentre] =
      val result =
        for query <- SpatialQueryConverter.from(request)
        yield manager.search(query)

      ZStream
        .logDebug("Searhing for epicentres...")
        .mapZIO(_ => result)
        .flatten
        .mapZIO(GRPCEpicentreConverter.from)
        .tapErrorCause(ZIO.logWarningCause("It was impossible to search or continue searching epicentres!", _))
        .mapError(mapSearchEpicentreError)

    override def searchHypocentre(request: HypocentreQuery, context: RequestContext): Stream[Status, GRPCHypocentre] =
      val result =
        for query <- SpatialQueryConverter.from(request)
        yield manager.search(query)

      ZStream
        .logDebug("Searching for hypocentres...")
        .mapZIO(_ => result)
        .flatten
        .mapZIO(GRPCHypocentreConverter.from)
        .tapErrorCause(ZIO.logWarningCause("It was impossible to search or continue searching hypocentres!", _))
        .mapError(mapSearchHypocentreError)

    private def mapSearchEpicentreError(cause: Throwable): Status = Status.INTERNAL

    private def mapSearchHypocentreError(cause: Throwable): Status = Status.INTERNAL
