package webapi1x.crawler

import graboid.protocol.CrawlerDescriptor
import zio.Task
import zio.ULayer
import zio.ZIO
import zio.kafka.producer.Producer

trait CrawlerManager:

  def create(descriptor: CrawlerDescriptor): Task[CrawlerDescriptor]

object CrawlerManager

private[crawler] class CrawlerManagerImpl(
    producerLayer: ULayer[Producer]
) extends CrawlerManager:

  override def create(descriptor: CrawlerDescriptor): Task[CrawlerDescriptor] = ???
