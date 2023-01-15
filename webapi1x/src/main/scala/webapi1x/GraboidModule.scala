package webapi1x

import com.softwaremill.macwire.wire
import webapi1x.WebApi.WebApiConfig
import zio.Task
import zio.ZIO

trait GraboidModule

object GraboidModule:

  def apply(config: WebApiConfig, kafkaModule: KafkaModule): Task[GraboidModule] =
    ZIO.attempt(wire[GraboidModuleImpl])

private[webapi1x] class GraboidModuleImpl(config: WebApiConfig, kafkaModule: KafkaModule) extends GraboidModule
