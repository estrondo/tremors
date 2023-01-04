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
import farango.FarangoDocumentCollection
import graboid.arango.ArangoRepository

trait CrawlerRepository:

  def add(descriptor: CrawlerDescriptor): Task[CrawlerDescriptor]

  def get(key: String): Task[Option[CrawlerDescriptor]]

  def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor]

  def remove(key: String): Task[Option[CrawlerDescriptor]]

  def update(key: String, descriptor: UpdateCrawlerDescriptor): Task[Option[CrawlerDescriptor]]

object CrawlerRepository:

  private[graboid] val CollectionNane = "crawler"

  def apply(database: FarangoDatabase): Task[CrawlerRepository] =
    for collection <- database.documentCollection(CollectionNane)
    yield CrawlerRepositoryImpl(collection)

  private[graboid] case class MappedCrawlerDescriptor(
      _key: String,
      name: String,
      `type`: String,
      source: String,
      windowDuration: Long,
      starting: Long
  )

  private[graboid] given toMapped: Conversion[CrawlerDescriptor, MappedCrawlerDescriptor] =
    descriptor =>
      MappedCrawlerDescriptor(
        _key = descriptor.key,
        name = descriptor.name,
        `type` = descriptor.`type`,
        source = descriptor.source,
        windowDuration = descriptor.windowDuration,
        starting = descriptor.starting
      )

  private[graboid] given fromMapped: Conversion[MappedCrawlerDescriptor, CrawlerDescriptor] =
    mapped =>
      CrawlerDescriptor(
        key = mapped._key,
        name = mapped.name,
        `type` = mapped.`type`,
        source = mapped.source,
        windowDuration = mapped.windowDuration,
        starting = mapped.starting
      )

private[graboid] class CrawlerRepositoryImpl(collection: FarangoDocumentCollection)
    extends CrawlerRepository:

  import CrawlerRepository.given

  private val repository = ArangoRepository[MappedCrawlerDescriptor](collection)

  override def add(descriptor: CrawlerDescriptor): Task[CrawlerDescriptor] =
    repository.add(descriptor)

  override def get(key: String): Task[Option[CrawlerDescriptor]] =
    repository.get(key)

  override def getAllDescriptors(): ZStream[Any, Throwable, CrawlerDescriptor] =
    for mapped <- collection.loadAll[MappedCrawlerDescriptor, Ziorango.S]
    yield fromMapped(mapped)

  override def remove(key: String): Task[Option[CrawlerDescriptor]] =
    repository.remove(key)

  override def update(
      key: String,
      descriptor: UpdateCrawlerDescriptor
  ): Task[Option[CrawlerDescriptor]] =
    for
      previous <- repository.getT(key)
      output   <- previous match
                    case Some(value) => update(key, value, descriptor)
                    case None        => ZIO.none
    yield output

  private def update(
      key: String,
      oldValue: MappedCrawlerDescriptor,
      descriptor: UpdateCrawlerDescriptor
  ): Task[Option[CrawlerDescriptor]] =
    var updated = oldValue
    updated = updateField(updated, descriptor.name, (e, f) => e.copy(name = f))
    updated = updateField(updated, descriptor.source, (e, f) => e.copy(source = f))
    updated = updateField(updated, descriptor.`type`, (e, f) => e.copy(`type` = f))
    updated = updateField(updated, descriptor.starting, (e, f) => e.copy(starting = f))
    updated = updateField(updated, descriptor.windowDuration, (e, f) => e.copy(windowDuration = f))
    repository.update(key, updated)

  private inline def updateField[T, F](entity: T, newValue: Option[F], inline fn: (T, F) => T): T =
    if (newValue.isDefined) then fn(entity, newValue.get) else entity
