package webapi1x

import com.softwaremill.macwire.wire
import webapi1x.WebApi.WebApiConfig
import zio.Task
import zio.ZIO
import webapi1x.graboid.GraboidCommandDispatcher
import webapi1x.handler.GraboidHandler

trait GraboidModule:

  def crawlerManager: GraboidCommandDispatcher

  def crawlerHandler: GraboidHandler

object GraboidModule:

  def apply(config: WebApiConfig, kafkaModule: KafkaModule): Task[GraboidModule] =
    ZIO.attempt(wire[GraboidModuleImpl])

private[webapi1x] class GraboidModuleImpl(config: WebApiConfig, kafkaModule: KafkaModule)
    extends GraboidModule:

  override val crawlerManager: GraboidCommandDispatcher = ???

  override val crawlerHandler: GraboidHandler = GraboidHandler(crawlerManager)
