package graboid

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.fromConversion
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import zio.Task
import zio.ZIO
import zio.stream.ZStream

import java.time.ZonedDateTime

trait CrawlerExecutionRepository:

  def add(execution: CrawlerExecution): Task[CrawlerExecution]

  def searchLast(publisher: Publisher): Task[Option[CrawlerExecution]]

  def update(execution: CrawlerExecution): Task[Option[CrawlerExecution]]

  def removeWithPublisherKey(publisherKey: String): Task[ZStream[Any, Throwable, CrawlerExecution]]

object CrawlerExecutionRepository:

  def apply(collection: DocumentCollection): CrawlerExecutionRepository =
    wire[Impl]

  private val QueryLastExecution = """  |FOR doc IN @@collection
                                        | FILTER doc.publisherKey == @publisherKey
                                        | SORT doc.ending DESC
                                        | LIMIT 1
                                        | RETURN doc
""".stripMargin

  private val RemoveWithPublisherKeyQuery = """ |FOR doc IN @@collection
                                                | FILTER doc.publisherKey == @publisherKey
                                                | REMOVE doc._key IN @@collection
                                                | RETURN doc
""".stripMargin

  private[graboid] case class Document(
      _key: String,
      publisherKey: String,
      beginning: ZonedDateTime,
      ending: ZonedDateTime,
      status: Option[Int],
      executionStarted: Option[ZonedDateTime],
      expectedStop: Option[ZonedDateTime],
      executionStopped: Option[ZonedDateTime],
      message: Option[String]
  )

  private[graboid] case class UpdateDocument(
      status: Option[Int],
      executionStarted: Option[ZonedDateTime],
      expectedStop: Option[ZonedDateTime],
      executionStopped: Option[ZonedDateTime],
      message: Option[String]
  )

  private[graboid] given Conversion[CrawlerExecution, Document] =
    _.into[Document]
      .transform(
        Field.renamed(_._key, _.key)
      )

  private[graboid] given Conversion[Document, CrawlerExecution] =
    _.into[CrawlerExecution]
      .transform(
        Field.renamed(_.key, _._key)
      )

  private[graboid] given Conversion[CrawlerExecution, UpdateDocument] =
    _.into[UpdateDocument]
      .transform()

  private class Impl(collection: DocumentCollection) extends CrawlerExecutionRepository:

    private def database = collection.database

    def add(execution: CrawlerExecution): Task[CrawlerExecution] =
      collection.insert[Document](execution) <* ZIO.logInfo(
        s"Added execution=${execution.key} for publisher=${execution.publisherKey}."
      )

    override def removeWithPublisherKey(publisherKey: String): Task[ZStream[Any, Throwable, CrawlerExecution]] =
      database.query[Document](
        RemoveWithPublisherKeyQuery,
        Map(
          "@collection"  -> collection.name,
          "publisherKey" -> publisherKey
        )
      )

    def searchLast(publisher: Publisher): Task[Option[CrawlerExecution]] =
      for
        stream <- database.query[Document](
                    QueryLastExecution,
                    Map(
                      "@collection"  -> collection.name,
                      "publisherKey" -> publisher.key
                    )
                  )
        head   <- stream.runHead
      yield head

    override def update(execution: CrawlerExecution): Task[Option[CrawlerExecution]] =
      collection.update[UpdateDocument, Document](execution.key, execution) <* ZIO.logDebug(
        s"There was an attempt to update execution=${execution.key}."
      )
