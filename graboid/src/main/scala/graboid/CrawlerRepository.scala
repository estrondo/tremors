package graboid

import farango.FarangoDatabase
import graboid.arango.ArangoConversion.given
import zio.Task
import zio.stream.ZStream
import ziorango.Ziorango
import ziorango.given

import CrawlerRepository.*
import graboid.protocol.CrawlerDescriptor
import graboid.protocol.UpdateCrawlerDescriptor
import zio.ZIO

trait CrawlerRepository:

  def add(descriptor: CrawlerDescriptor): Task[CrawlerDescriptor]

  def get(key: String): Task[Option[CrawlerDescriptor]]

  def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor]

  def remove(key: String): Task[Option[CrawlerDescriptor]]

  def update(key: String, descriptor: UpdateCrawlerDescriptor): Task[Option[CrawlerDescriptor]]

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

  override def get(key: String): Task[Option[CrawlerDescriptor]] = ???

  override def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor] =
    for mapped <- collection.loadAll[MappedCrawlerDescriptor, Ziorango.S]
    yield toCrawlerDescriptor(mapped)

  override def remove(key: String): Task[Option[CrawlerDescriptor]] =
    for old <- collection.remove[MappedCrawlerDescriptor, Ziorango.F](key)
    yield old.map(toCrawlerDescriptor)

  override def update(
      key: String,
      descriptor: UpdateCrawlerDescriptor
  ): Task[Option[CrawlerDescriptor]] =
    for
      oldCrawler <- collection.get[MappedCrawlerDescriptor, Ziorango.F](key)
      newCrawler <- oldCrawler match
                      case Some(value) => update(key, value, descriptor)
                      case None        => ZIO.none
    yield newCrawler.map(toCrawlerDescriptor(_))

  private def update(
      key: String,
      oldValue: MappedCrawlerDescriptor,
      descriptor: UpdateCrawlerDescriptor
  ): Task[Option[MappedCrawlerDescriptor]] =
    var updated = oldValue
    updated = updateField(updated, descriptor.name, (e, f) => e.copy(name = f))
    updated = updateField(updated, descriptor.source, (e, f) => e.copy(source = f))
    updated = updateField(updated, descriptor.`type`, (e, f) => e.copy(`type` = f))
    updated = updateField(updated, descriptor.starting, (e, f) => e.copy(starting = f))
    updated = updateField(updated, descriptor.windowDuration, (e, f) => e.copy(windowDuration = f))
    collection.update[MappedCrawlerDescriptor, Ziorango.F](key, updated)

  private inline def updateField[T, F](entity: T, newValue: Option[F], inline fn: (T, F) => T): T =
    if (newValue.isDefined) then fn(entity, newValue.get) else entity
