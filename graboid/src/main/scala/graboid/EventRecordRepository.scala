package graboid

import com.softwaremill.macwire.wire
import farango.FarangoDocumentCollection
import graboid.arango.ArangoConversion.given
import graboid.arango.ArangoRepository
import graboid.arango.createZonedDateTime
import graboid.query.TimeWindowLink
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import zio.Task
import zio.stream.Stream
import ziorango.Ziorango
import ziorango.given

import java.time.ZonedDateTime

trait EventRecordRepository:

  def add(record: EventRecord): Task[EventRecord]

  def searchByPublisher(
      publisherKey: String,
      timeWindowLink: Option[TimeWindowLink] = None
  ): Task[Stream[Throwable, EventRecord]]

  def update(record: EventRecord): Task[EventRecord]

object EventRecordRepository:

  def apply(collection: FarangoDocumentCollection): EventRecordRepository =
    wire[EventRecordRepositoryImpl]

  private val QueryByPublisher = """ |FOR d IN @@collection
                                     | FILTER d.publisherKey == @publisherKey
                                     |RETURN d
""".stripMargin

  private val QueryByPublisherUnliked = """ |FOR d IN @@collection
                                            | FILTER d.publisherKey == @publisherKey
                                            | FILTER d.timeWindowKey == null
                                            |RETURN d
""".stripMargin

  private val QueryByPublisherAndLinked = """ |FOR d IN @@collection
                                              | FILTER d.publisherKey == @publisherKey
                                              | FILTER d.timeWindowKey == @timeWindowKey
                                              |RETURN d
""".stripMargin

  private[graboid] case class Document(
      _key: String,
      publisherKey: String,
      message: String,
      eventInstant: Long,
      timeWindowKey: String
  )

  private[graboid] given Conversion[EventRecord, Document] =
    _.into[Document]
      .transform(
        Field.renamed(_._key, _.key)
      )

  private[graboid] given documentToEventRecord: Conversion[Document, EventRecord] =
    _.into[EventRecord]
      .transform(
        Field.renamed(_.key, _._key)
      )

  private class EventRecordRepositoryImpl(collection: FarangoDocumentCollection) extends EventRecordRepository:

    val repository = ArangoRepository[Document](collection)

    def database = repository.database

    override def add(record: EventRecord): Task[EventRecord] =
      repository.add(record).mapError(handleAddError(record))

    override def searchByPublisher(
        publisherKey: String,
        timeWindow: Option[TimeWindowLink] = None
    ): Task[Stream[Throwable, EventRecord]] =
      val (query, args) = timeWindow match
        case Some(TimeWindowLink.With(key)) =>
          (QueryByPublisherAndLinked, Map("timeWindowKey" -> key))

        case Some(TimeWindowLink.Unliked) =>
          (QueryByPublisherUnliked, Map.empty)

        case None =>
          (QueryByPublisher, Map.empty)

      database
        .queryT[Document, EventRecord, Ziorango.F, Ziorango.S](
          query,
          args ++ Map(
            "@collection"  -> collection.name,
            "publisherKey" -> publisherKey
          )
        )

    override def update(record: EventRecord): Task[EventRecord] = ???

    private def handleAddError(record: EventRecord)(cause: Throwable) =
      GraboidException.IllegalState(s"It was impossible to add EventRecord: ${record.key}", cause)
