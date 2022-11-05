package graboid

import farango.FarangoDatabase
import graboid.arango.ArangoConversion.given
import zio.Task
import zio.stream.ZStream
import ziorango.Ziorango
import ziorango.given

import CrawlerRepository.*
import graboid.protocol.CrawlerDescriptor

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
      _key: String,
      name: String,
      `type`: String,
      source: String,
      windowDuration: Long,
      starting: Long
  )

  private[graboid] def toMappedCrawlerDescriptor(
      descriptor: CrawlerDescriptor
  ): MappedCrawlerDescriptor = MappedCrawlerDescriptor(
    _key = descriptor.key,
    name = descriptor.name,
    `type` = descriptor.`type`,
    source = descriptor.source,
    windowDuration = descriptor.windowDuration,
    starting = descriptor.starting
  )

  private[graboid] def toCrawlerDescriptor(
      mapped: MappedCrawlerDescriptor
  ): CrawlerDescriptor = CrawlerDescriptor(
    key = mapped._key,
    name = mapped.name,
    `type` = mapped.`type`,
    source = mapped.source,
    windowDuration = mapped.windowDuration,
    starting = mapped.starting
  )

private[graboid] class CrawlerRepositoryImpl(database: FarangoDatabase) extends CrawlerRepository:

  private val collection = database.documentCollection("crawlers")

  override def add(descriptor: CrawlerDescriptor): Task[CrawlerDescriptor] =
    for stored <- collection.insert(toMappedCrawlerDescriptor(descriptor))
    yield toCrawlerDescriptor(stored)

  override def get(name: String): Task[Option[CrawlerDescriptor]] = ???

  override def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor] =
    for mapped <- collection.loadAll[MappedCrawlerDescriptor, Ziorango.S]
    yield toCrawlerDescriptor(mapped)

  override def remove(name: String): Task[Option[CrawlerDescriptor]] =
    for old <- collection.remove[MappedCrawlerDescriptor, Ziorango.F](name)
    yield old.map(toCrawlerDescriptor)

  override def update(descriptor: CrawlerDescriptor): Task[Option[CrawlerDescriptor]] =
    for old <- collection.update(descriptor.key, toMappedCrawlerDescriptor(descriptor))
    yield old.map(toCrawlerDescriptor)
