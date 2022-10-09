package tremors.graboid.repository

import farango.FarangoDatabase
import tremors.graboid.TimelineManager.Window
import tremors.graboid.arango.ArangoConversion.given
import tremors.graboid.repository.TimelineRepository.*
import zio.Task
import zio.stream.ZSink
import ziorango.Ziorango
import ziorango.given

import java.time.ZonedDateTime

trait TimelineRepository:

  def add(name: String, beginning: ZonedDateTime, ending: ZonedDateTime): Task[Window]

  def last(name: String): Task[Option[Window]]

object TimelineRepository:

  private[repository] val CrawlerNameParam = "crawlerName"

  private[repository] val CollectionName = "crawlerTimeline"

  private[repository] val QueryLastWindow =
    s"FOR w IN $CollectionName FILTER w.name == @$CrawlerNameParam SORT w.ending DESC LIMIT 1 RETURN w"

  private[repository] case class MappedWindow(name: String, beginning: Long, ending: Long)

  def apply(database: FarangoDatabase): TimelineRepository = TimelineRepositoryImpl(database)

private class TimelineRepositoryImpl(database: FarangoDatabase) extends TimelineRepository:

  private val collection = database.documentCollection(CollectionName)

  override def add(name: String, beginning: ZonedDateTime, ending: ZonedDateTime): Task[Window] =
    val window = MappedWindow(
      name,
      beginning,
      ending
    )

    for _ <- collection.insert(window)
    yield (beginning, ending)

  override def last(name: String): Task[Option[Window]] =
    for
      stream <- database.query[MappedWindow, Task, Ziorango.S](
                  QueryLastWindow,
                  Map(CrawlerNameParam -> name)
                )
      head   <- stream.run(ZSink.head)
    yield head.map(convertToWindow)

  private def convertToWindow(window: MappedWindow): Window =
    (window.beginning, window.ending)
