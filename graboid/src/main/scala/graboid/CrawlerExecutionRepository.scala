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

trait CrawlerExecutionRepository:

  def add(execution: CrawlerExecution): Task[CrawlerExecution]

  def searchLast(publisher: Publisher): Task[Option[CrawlerExecution]]

object CrawlerExecutionRepository:

  def apply(collection: DocumentCollection): CrawlerExecutionRepository =
    wire[CralwerExecutionRepositoryImpl]

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

  given Conversion[CrawlerExecution, Document] =
    _.into[Document]
      .transform(
        Field.renamed(_._key, _.key)
      )

  private class CralwerExecutionRepositoryImpl(collection: DocumentCollection) extends CrawlerExecutionRepository:

    def add(execution: CrawlerExecution): Task[CrawlerExecution] =
      collection.insert[Document](execution) <* ZIO.logInfo(
        s"Added execution=${execution.key} for publisherKey=${execution.publisherKey}."
      )

    def searchLast(publisher: Publisher): Task[Option[CrawlerExecution]] = ???
