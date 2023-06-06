package toph.module

import com.softwaremill.macwire.wire
import io.grpc.ServerBuilder
import scalapb.zio_grpc.ServerLayer
import scalapb.zio_grpc.ServiceList
import toph.config.GRPCConfig
import zio.Task
import zio.ZIO

trait GRPCModule:

  def run(): Task[Unit]

object GRPCModule:

  def apply(config: GRPCConfig, serviceModule: ServiceModule): Task[GRPCModule] =
    ZIO.attempt(wire[Impl])

  private class Impl(config: GRPCConfig, serviceModule: ServiceModule) extends GRPCModule:

    override def run(): Task[Unit] =
      val services = ServiceList
        .add(serviceModule.spatailService)

      ServerLayer
        .fromServiceList(
          ServerBuilder.forPort(config.port),
          services
        )
        .launch
