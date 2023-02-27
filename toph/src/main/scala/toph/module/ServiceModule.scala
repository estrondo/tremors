package toph.module

import com.softwaremill.macwire.wire
import scalapb.zio_grpc.RequestContext
import grpc.toph.spatial.ZioSpatial.ZSpatialService
import toph.service.SpatialService
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer

trait ServiceModule:

  val spatailService: ZSpatialService[RequestContext]

object ServiceModule:

  def apply(coreModule: CoreModule): Task[ServiceModule] =
    ZIO.attempt(wire[Impl])

  private class Impl(coreModule: CoreModule) extends ServiceModule:

    override val spatailService: ZSpatialService[RequestContext] =
      SpatialService(coreModule.spatialManager)