package tremors.graboid

import zio.stream.ZStream

import CrawlerRepository.*

trait CrawlerRepository:

  def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor]

object CrawlerRepository

private[graboid] class CrawlerRepositoryImpl extends CrawlerRepository:

  override def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor] = ???
