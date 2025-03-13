package toph.module

import io.grpc.Grpc
import io.grpc.InsecureServerCredentials
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.Server
import scalapb.zio_grpc.ServerLayer
import scalapb.zio_grpc.ServiceList
import toph.TimeService
import toph.config.GrpcConfig
import toph.grpc.impl.GrpcAccountService
import toph.grpc.impl.GrpcObjectStorageService
import toph.grpc.impl.GrpcSecurityService
import toph.grpc.impl.convertRequestContextToAccessToken
import toph.v1.grpc.ZioGrpc
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer

class GrpcModule(val server: TaskLayer[Server])

object GrpcModule:

  def apply(config: GrpcConfig, securityModule: SecurityModule, centreModule: CentreModule): Task[GrpcModule] =
    val accessTokenTransformer = convertRequestContextToAccessToken(securityModule.tokenCodec, TimeService)

    val accountServiceLayer: TaskLayer[ZioGrpc.ZAccountService[RequestContext]] =
      ZLayer {
        for service <- GrpcAccountService(centreModule.accountService)
        yield service.transformContextZIO(accessTokenTransformer)
      }

    val objectStorageServiceLayer = ZLayer.succeed {
      GrpcObjectStorageService(
        systemReadService = centreModule.systemUserReadStorageService,
        userReadService = centreModule.userReadStorageService,
        userUpdateService = centreModule.userUpdateStorageService,
      ).transformContextZIO(accessTokenTransformer)
    }

    val securityLayer = ZLayer {
      GrpcSecurityService(securityModule.securityCentre)
    }

    val serverLayer = ServerLayer.fromServiceList(
      Grpc.newServerBuilderForPort(config.port, InsecureServerCredentials.create()),
      ServiceList
        .addFromEnvironment[ZioGrpc.ZAccountService[RequestContext]]
        .addFromEnvironment[ZioGrpc.ZSecurityService[RequestContext]]
        .addFromEnvironment[ZioGrpc.ZObjectStorageService[RequestContext]],
    )

    ZIO.succeed(
      new GrpcModule(
        server = ZLayer
          .make[Server](
            serverLayer,
            accountServiceLayer,
            securityLayer,
            objectStorageServiceLayer,
          )
          .tap { env =>
            env.get.port.tap { port =>
              ZIO.logInfo(s"🌎Toph is listening @ $port.")
            }
          },
      ),
    )
