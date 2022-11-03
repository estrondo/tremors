package tremors.webapi1x

import com.softwaremill.macwire.wire
import tremors.webapi1x.WebApi.WebApiConfig
import tremors.webapi1x.crawler.CrawlerManager
import tremors.webapi1x.handler.CrawlerHandler
import zio.Task
import zio.ZIO

trait CrawlerModule:

  def crawlerManager: CrawlerManager

  def crawlerHandler: CrawlerHandler

object CrawlerModule:

  def apply(config: WebApiConfig, kafkaModule: KafkaModule): Task[CrawlerModule] =
    ZIO.attempt(wire[CrawlerModuleImpl])

private[webapi1x] class CrawlerModuleImpl(config: WebApiConfig, kafkaModule: KafkaModule)
    extends CrawlerModule:

  override val crawlerManager: CrawlerManager = ???

  override val crawlerHandler: CrawlerHandler = CrawlerHandler(crawlerManager)
