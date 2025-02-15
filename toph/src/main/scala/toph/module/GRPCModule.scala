package toph.module

import io.grpc.Grpc
import io.grpc.InsecureServerCredentials
import scalapb.zio_grpc.RequestContext
import scalapb.zio_grpc.Server
import scalapb.zio_grpc.ServerLayer
import scalapb.zio_grpc.ServiceList
import toph.config.GRPCConfig
import toph.grpc.impl.GRPCAccountService
import toph.grpc.impl.GRPCSecurityService
import toph.grpc.impl.convertRequestContextToToken
import toph.v1.grpc.ZioGrpc
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer

class GRPCModule(val server: TaskLayer[Server])

object GRPCModule:

  def apply(config: GRPCConfig, securityModule: SecurityModule, centreModule: CentreModule): Task[GRPCModule] =
    val tokenTransformer = convertRequestContextToToken(securityModule.tokenService)

    val accountServiceLayer: TaskLayer[ZioGrpc.ZAccountService[RequestContext]] =
      ZLayer {
        for service <- GRPCAccountService(centreModule.accountService)
        yield service.transformContextZIO(tokenTransformer)
      }

    val securityLayer = ZLayer {
      GRPCSecurityService(securityModule.securityCentre)
    }

    val serverLayer = ServerLayer.fromServiceList(
      Grpc.newServerBuilderForPort(config.port, InsecureServerCredentials.create()),
      ServiceList
        .addFromEnvironment[ZioGrpc.ZAccountService[RequestContext]]
        .addFromEnvironment[ZioGrpc.ZSecurityService[RequestContext]],
    )

    ZIO.succeed(
      new GRPCModule(
        server = ZLayer
          .make[Server](
            serverLayer,
            accountServiceLayer,
            securityLayer,
          )
          .tap { env =>
            env.get.port.tap { port =>
              ZIO.logInfo(s"🌎Toph is listening @ $port.")
            }
          },
      ),
    )
