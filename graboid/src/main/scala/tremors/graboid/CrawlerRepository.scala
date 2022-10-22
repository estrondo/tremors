package tremors.graboid

import zio.stream.ZStream

import CrawlerRepository.*
import farango.FarangoDatabase

trait CrawlerRepository:

  def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor]

object CrawlerRepository:

  def apply(database: FarangoDatabase): CrawlerRepository =
    CrawlerRepositoryImpl(database)

private[graboid] class CrawlerRepositoryImpl(database: FarangoDatabase) extends CrawlerRepository:

  private val collection = database.documentCollection("crawler-repository")

  override def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor] =
    throw IllegalStateException("getAllDescriptors")
