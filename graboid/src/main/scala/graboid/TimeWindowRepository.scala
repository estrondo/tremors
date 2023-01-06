package graboid

import com.softwaremill.macwire.wire
import farango.FarangoDatabase
import farango.FarangoDocumentCollection
import graboid.arango.ArangoConversion.given
import graboid.arango.ArangoRepository
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import zio.Task
import zio.stream.ZSink
import ziorango.Ziorango
import ziorango.given

import java.time.ZonedDateTime

trait TimeWindowRepository:

  def database: FarangoDatabase

  def add(window: TimeWindow): Task[TimeWindow]

  def search(
      publisherKey: String,
      beginning: ZonedDateTime,
      ending: ZonedDateTime
  ): Task[Option[TimeWindow]]

  def update(window: TimeWindow): Task[TimeWindow]

object TimeWindowRepository:

  def apply(collection: FarangoDocumentCollection): TimeWindowRepository =
    wire[TimeWindowRepositoryImpl]

  private[graboid] case class Document(
      _key: String,
      publisherKey: String,
      beginning: Long,
      ending: Long,
      successes: Long,
      failures: Long
  )

  private[graboid] given timeWindowToDocument: Conversion[TimeWindow, Document] =
    _.into[Document]
      .transform(
        Field.renamed(_._key, _.key)
      )

  private[graboid] given documentToTimeWindow: Conversion[Document, TimeWindow] =
    _.into[TimeWindow]
      .transform(
        Field.renamed(_.key, _._key)
      )

  private val QuerySearchWindow = """ FOR d IN @@collection
                                    |   FILTER d.publisherKey == @publisherKey
                                    |   FILTER d.beginning <= @beginning
                                    |   FILTER d.ending > @ending
                                    | RETURN d
""".stripMargin

  private class TimeWindowRepositoryImpl(collection: FarangoDocumentCollection)
      extends TimeWindowRepository:

    val repository = ArangoRepository[Document](collection)

    override def database: FarangoDatabase = repository.database

    override def add(window: TimeWindow): Task[TimeWindow] =
      repository.add(window).mapError(handleAddWindowError(window))

    override def search(
        publisherKey: String,
        beginning: ZonedDateTime,
        ending: ZonedDateTime
    ): Task[Option[TimeWindow]] =
      for
        stream <- database.query[Document, Ziorango.F, Ziorango.S](
                    QuerySearchWindow,
                    Map(
                      "@collection"  -> collection.name,
                      "publisherKey" -> publisherKey,
                      "beginning"    -> (zonedDateTimeToLong.transform(beginning)),
                      "ending"       -> (zonedDateTimeToLong.transform(ending))
                    )
                  )
        head   <- stream.run(ZSink.head)
      yield head.map(documentToTimeWindow)

    override def update(window: TimeWindow): Task[TimeWindow] = ???

    def handleAddWindowError(window: TimeWindow)(cause: Throwable) =
      GraboidException.IllegalState(s"It was impossible to add TimeWindow: ${window.key}.", cause)
