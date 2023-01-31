package graboid

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.ArangoConversion.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import zio.Task

import java.lang.{Long => JLong, Integer => JInt}
import zio.ZIO
import zio.stream.ZStream
import java.time.ZonedDateTime

trait CrawlerExecutionRepository:

  def add(execution: CrawlerExecution): Task[CrawlerExecution]

  def searchLast(publisher: Publisher): Task[Option[CrawlerExecution]]

object CrawlerExecutionRepository:

  def apply(collection: DocumentCollection): CrawlerExecutionRepository =
    wire[Impl]

  private val QueryLastExecution = """  |FOR doc IN @@collection
                                        | FILTER doc.publisherKey == @publisherKey
                                        | SORT doc.ending DESC
                                        | LIMIT 1
                                        | RETURN doc
  """.stripMargin

  private[graboid] case class Document(
      _key: String,
      publisherKey: String,
      beginning: Long,
      ending: Long,
      status: JInt,
      executionStarted: JLong,
      expectedStop: JLong,
      executionStopped: JLong,
      message: String
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

  private class Impl(collection: DocumentCollection) extends CrawlerExecutionRepository:

    private def database = collection.database

    def add(execution: CrawlerExecution): Task[CrawlerExecution] =
      collection.insert[Document](execution) <* ZIO.logInfo(
        s"Added execution=${execution.key} for publisherKey=${execution.publisherKey}."
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
