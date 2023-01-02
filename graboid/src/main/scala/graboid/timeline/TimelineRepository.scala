package graboid.repository

import farango.FarangoDatabase
import graboid.TimelineManager.Window
import graboid.arango.ArangoConversion.given
import graboid.repository.TimelineRepository.*
import zio.Task
import zio.stream.ZSink
import ziorango.Ziorango
import ziorango.given
import zio.TaskLayer

import java.time.ZonedDateTime
import farango.FarangoDocumentCollection

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

private class TimelineRepositoryImpl(collection: FarangoDocumentCollection)
    extends TimelineRepository:

  private def database = collection.database

  override def add(name: String, window: Window): Task[Window] =
    val mappedWindow = MappedWindow(
      name,
      window.id,
      window.beginning,
      window.ending
    )

    for _ <- collection.insert(mappedWindow)
    yield window

  override def last(name: String): Task[Option[Window]] =
    for
      stream <- database.query[MappedWindow, Task, Ziorango.S](
                  QueryLastWindow,
                  Map(CrawlerNameParam -> name)
                )
      head   <- stream.run(ZSink.head)
    yield head.map(convertToWindow)

  private def convertToWindow(mappedWindow: MappedWindow): Window =
    Window(
      id = mappedWindow.id,
      beginning = mappedWindow.beginning,
      ending = mappedWindow.ending
    )
