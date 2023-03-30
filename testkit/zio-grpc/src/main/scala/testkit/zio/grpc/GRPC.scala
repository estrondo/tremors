package testkit.zio.grpc

import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import java.util.concurrent.atomic.AtomicInteger
import scala.util.Random
import scalapb.zio_grpc.Server
import scalapb.zio_grpc.ServerLayer
import scalapb.zio_grpc.ZBindableService
import scalapb.zio_grpc.ZChannel
import scalapb.zio_grpc.ZManagedChannel
import zio.RIO
import zio.RLayer
import zio.Scope
import zio.Tag
import zio.ZIO

object GRPC:

  private val nextPort = AtomicInteger(Random.nextInt(1024) + 4 * 1024)

  def createChannel: RIO[Server, RIO[Scope, ZChannel]] =
    for
      port   <- ZIO.serviceWithZIO[Server](_.port)
      result <- ZIO.succeed(
                  ZManagedChannel(
                    ManagedChannelBuilder
                      .forAddress("127.0.0.1", port)
                      .usePlaintext()
                  )
                )
    yield result

  def serverLayerFor[T: ZBindableService: Tag]: RLayer[T & Scope, Server] =
    ServerLayer
      .fromEnvironment(ServerBuilder.forPort(nextPort.getAndIncrement()))
