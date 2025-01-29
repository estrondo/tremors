package tremors.zio.grpc

import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import io.grpc.ServerBuilder
import scalapb.zio_grpc.GeneratedClient
import scalapb.zio_grpc.Server
import scalapb.zio_grpc.ServerLayer
import scalapb.zio_grpc.ServiceList
import scalapb.zio_grpc.ZBindableService
import scalapb.zio_grpc.ZManagedChannel
import zio.RLayer
import zio.Tag
import zio.TaskLayer
import zio.ZIO
import zio.ZLayer

import java.net.ServerSocket

object GrpcTestableService:

  def serverOf[S: ZBindableService: Tag]: RLayer[S, Server] =
    ZLayer
      .fromZIO(ZIO.attemptBlocking {
        ServerSocket(0).getLocalPort
      })
      .flatMap { env =>
        ServerLayer
          .fromServiceList(
            ServerBuilder
              .forPort(env.get[Int]),
            ServiceList.addFromEnvironment[S],
          )
      }

  def clientOf[C <: GeneratedClient[_]](f: ZManagedChannel => TaskLayer[C]): RLayer[Server, C] =
    ZLayer
      .environment[Server]
      .flatMap { env =>
        ZLayer.fromZIO(env.get[Server].port)
      }
      .flatMap[Server, Throwable, C] { env =>
        f(
          ZManagedChannel(
            Grpc.newChannelBuilderForAddress("127.0.0.1", env.get[Int], InsecureChannelCredentials.create()),
          ),
        )
      }
