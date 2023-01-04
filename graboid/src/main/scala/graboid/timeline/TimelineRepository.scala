package graboid.repository

import farango.FarangoDatabase
import farango.FarangoDocumentCollection
import graboid.TimelineManager.Window
import graboid.arango.ArangoConversion.given
import graboid.arango.ArangoRepository
import zio.Task
import zio.TaskLayer
import zio.stream.ZSink
import ziorango.Ziorango
import ziorango.given

import java.time.ZonedDateTime

import TimelineRepository.*
import TimelineRepository.given

trait TimelineRepository:

  def add(name: String, window: Window): Task[Window]

  def last(name: String): Task[Option[Window]]

object TimelineRepository:

  private[repository] val CrawlerNameParam = "crawlerName"

  private[repository] val CollectionName = "crawler_timeline"

  private[repository] val QueryLastWindow =
    s"FOR w IN $CollectionName FILTER w.name == @$CrawlerNameParam SORT w.ending DESC LIMIT 1 RETURN w"

  private[repository] case class MappedWindow(
      name: String,
      id: String,
      beginning: Long,
      ending: Long
  )

  def apply(database: FarangoDatabase): Task[TimelineRepository] =
    for collection <- database.documentCollection(CollectionName)
    yield TimelineRepository(collection)

  def apply(collection: FarangoDocumentCollection): TimelineRepository =
    TimelineRepositoryImpl(collection)

  private[graboid] given fromMapped: Conversion[MappedWindow, Window] =
    mappedWindow =>
      Window(
        id = mappedWindow.id,
        beginning = mappedWindow.beginning,
        ending = mappedWindow.ending
      )

private class TimelineRepositoryImpl(collection: FarangoDocumentCollection)
    extends TimelineRepository:

  private def database   = collection.database
  private val repository = ArangoRepository[MappedWindow](collection)

  override def add(name: String, window: Window): Task[Window] =
    val mappedWindow = MappedWindow(
      name,
      window.id,
      window.beginning,
      window.ending
    )

    repository.addT(mappedWindow)

  override def last(name: String): Task[Option[Window]] =
    for
      stream <- database.query[MappedWindow, Task, Ziorango.S](
                  QueryLastWindow,
                  Map(CrawlerNameParam -> name)
                )
      head   <- stream.run(ZSink.head)
    yield head.map(fromMapped)
