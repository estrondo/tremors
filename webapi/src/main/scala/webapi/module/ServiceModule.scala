package webapi.module

import core.KeyGenerator
import grpc.toph.spatial.ZioSpatial.{SpatialServiceClient => TophSpatialServiceClient}
import grpc.webapi.account.ZioAccount.ZAccountService
import grpc.webapi.spatial.ZioSpatial.ZSpatialService
import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.Server
import scalapb.zio_grpc.ServerLayer
import scalapb.zio_grpc.ServiceList
import scalapb.zio_grpc.ZChannel
import scalapb.zio_grpc.ZManagedChannel
import webapi.config.ServiceConfig
import webapi.config.SpatialServiceConfig
import webapi.model.UserClaims
import webapi.service.AccountService
import webapi.service.SpatialService
import zio.RIO
import zio.RLayer
import zio.Scope
import zio.Task
import zio.TaskLayer
import zio.ZLayer

class ServiceModule(
    val account: ZAccountService[UserClaims],
    val spatial: ZSpatialService[RequestContext],
    val server: TaskLayer[Server]
)

object ServiceModule:

  def apply(config: ServiceConfig, core: CoreModule, openIDModule: OpenIDModule): RIO[Scope, ServiceModule] =
    for
      spatial <- SpatialService().provideLayer(tophClient(config.spatial))
      account <- AccountService().provideSome(
                   ZLayer.succeed(KeyGenerator),
                   ZLayer.succeed(core.accountManager)
                 )
    yield new ServiceModule(account, spatial, createServer(config, openIDModule, spatial, account))

  private def tophClient(config: SpatialServiceConfig): RLayer[Scope, TophSpatialServiceClient] = ZLayer {
    TophSpatialServiceClient.scoped(createChannel(config.toph))
  }

  private def createChannel(uri: String): RIO[Scope, ZChannel] =
    ZManagedChannel(ManagedChannelBuilder.forTarget(uri).usePlaintext())

  private def createServer(
      config: ServiceConfig,
      openIDModule: OpenIDModule,
      spatial: ZSpatialService[RequestContext],
      account: ZAccountService[UserClaims]
  ): TaskLayer[Server] =
    ServerLayer
      .fromServiceList(
        ServerBuilder.forPort(config.port),
        ServiceList
          .add(spatial)
          .add(account.transformContextZIO(openIDModule.getUserClaims))
      )
