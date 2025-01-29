package toph.grpc

import scalapb.zio_grpc.Server
import toph.TophSpec
import toph.module.GRPCModule
import zio.RLayer
import zio.Scope
import zio.ZIO
import zio.ZLayer

abstract class TophGrpcSpec extends TophSpec
