package tremors.graboid

import farango.FarangoDatabase
import tremors.graboid.arango.ArangoConversion.given
import zio.Task
import zio.stream.ZStream
import ziorango.Ziorango
import ziorango.given

import CrawlerRepository.*

trait CrawlerRepository:

  def add(descriptor: CrawlerDescriptor): Task[CrawlerDescriptor]

  def get(name: String): Task[Option[CrawlerDescriptor]]

  def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor]

  def remove(name: String): Task[Option[CrawlerDescriptor]]

  def update(descriptor: CrawlerDescriptor): Task[Option[CrawlerDescriptor]]

object CrawlerRepository:

  def apply(database: FarangoDatabase): CrawlerRepository =
    CrawlerRepositoryImpl(database)

  private[graboid] case class MappedCrawlerDescriptor(
      name: String,
      `type`: String,
      source: String,
      windowDuration: Long,
      starting: Long
  )

  private[graboid] def toMapCrawlerDescriptor(
      descriptor: CrawlerDescriptor
  ): MappedCrawlerDescriptor = MappedCrawlerDescriptor(
    name = descriptor.name,
    `type` = descriptor.`type`,
    source = descriptor.source,
    windowDuration = descriptor.windowDuration,
    starting = descriptor.starting
  )

  private[graboid] def toCrawlerDescriptor(
      mapped: MappedCrawlerDescriptor
  ): CrawlerDescriptor = CrawlerDescriptor(
    name = mapped.name,
    `type` = mapped.`type`,
    source = mapped.source,
    windowDuration = mapped.windowDuration,
    starting = mapped.starting
  )

private[graboid] class CrawlerRepositoryImpl(database: FarangoDatabase) extends CrawlerRepository:

  private val collection = database.documentCollection("crawlers")

  override def add(descriptor: CrawlerDescriptor): Task[CrawlerDescriptor] =
    for stored <- collection.insert(toMapCrawlerDescriptor(descriptor))
    yield toCrawlerDescriptor(stored)

  override def get(name: String): Task[Option[CrawlerDescriptor]] = ???

  override def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor] =
    for mapped <- collection.loadAll[MappedCrawlerDescriptor, Ziorango.S]
    yield toCrawlerDescriptor(mapped)

  override def remove(name: String): Task[Option[CrawlerDescriptor]] = ???

  override def update(descriptor: CrawlerDescriptor): Task[Option[CrawlerDescriptor]] = ???
