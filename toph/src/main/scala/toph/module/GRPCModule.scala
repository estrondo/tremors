package toph.module

import io.grpc.ServerBuilder
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.Server
import scalapb.zio_grpc.ServerLayer
import scalapb.zio_grpc.ServiceList
import toph.config.GRPCConfig
import toph.service.UserService
import toph.service.ZioService.ZUserService
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer

trait GRPCModule:

  def server: TaskLayer[Server]

object GRPCModule:

  def apply(config: GRPCConfig, securityModule: SecurityModule, centreModule: CentreModule): Task[GRPCModule] =
    ZIO.succeed(Impl(config, securityModule, centreModule))

  private class Impl(config: GRPCConfig, securityModule: SecurityModule, centreModule: CentreModule) extends GRPCModule:

    private val userServiceLayer: TaskLayer[ZUserService[RequestContext]] =
      ZLayer {
        for service <- UserService(centreModule.userCentre)
        yield service.transformContextZIO(securityModule.securityCentre.authenticate)
      }

    override def server: TaskLayer[Server] =
      val serverList = ServiceList
        .addFromEnvironment[ZUserService[RequestContext]]

      val serverLayer = ServerLayer.fromServiceList(
        ServerBuilder.forPort(config.port),
        serverList
      )

      ZLayer
        .make[Server](
          serverLayer,
          userServiceLayer
        )
        .tap { env =>
          ZIO.logInfo(s"Toph is listening @ ${env.get.port}.")
        }
