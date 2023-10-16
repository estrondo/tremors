package graboid.repository

import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentUpdateOptions
import graboid.CrawlingExecution
import graboid.time.ZonedDateTimeService
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.Transformer
import java.time.ZonedDateTime
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer
import one.estrondo.farango.zio.given
import tremors.zio.farango.CollectionManager
import zio.Task

trait CrawlingExecutionRepository:

  def insert(execution: CrawlingExecution): Task[CrawlingExecution]

  def searchIntersection(
      dataCentreId: String,
      starting: ZonedDateTime,
      ending: ZonedDateTime
  ): Task[Seq[CrawlingExecution]]

  def updateCounting(execution: CrawlingExecution): Task[CrawlingExecution]

  def updateState(execution: CrawlingExecution): Task[CrawlingExecution]

object CrawlingExecutionRepository:

  private val IntersectionQuery =
    """FOR e IN @@collection
      | FILTER e.dataCentreId == @dataCentreId
      | FILTER (e.starting <= @starting && e.ending >= @ending) || (e.starting <= @starting && e.ending > @starting) || (e.starting < @ending && e.ending >= @ending)
      | RETURN e
      |""".stripMargin

  def apply(
      collectionManager: CollectionManager,
      zonedDateTimeService: ZonedDateTimeService
  ): CrawlingExecutionRepository =
    Impl(collectionManager, zonedDateTimeService)

  private given FarangoTransformer[CrawlingExecution, Stored] =
    DucktapeTransformer[CrawlingExecution, Stored](Field.renamed(_._key, _.id))

  private given FarangoTransformer[Stored, CrawlingExecution] =
    DucktapeTransformer[Stored, CrawlingExecution](Field.renamed(_.id, _._key))

  private given Transformer[CrawlingExecution.State, Int] = from => from.ordinal

  private given Transformer[Int, CrawlingExecution.State] = from => CrawlingExecution.State.fromOrdinal(from)

  case class Stored(
      _key: String,
      dataCentreId: String,
      createdAt: ZonedDateTime,
      updatedAt: Option[ZonedDateTime],
      starting: ZonedDateTime,
      ending: ZonedDateTime,
      detected: Long,
      state: Int
  )

  case class UpdateCounting(updatedAt: ZonedDateTime, detected: Long)

  case class UpdateState(updatedAt: ZonedDateTime, state: Int)

  private class Impl(collectionManager: CollectionManager, zonedDateTimeService: ZonedDateTimeService)
      extends CrawlingExecutionRepository:

    override def insert(execution: CrawlingExecution): Task[CrawlingExecution] =
      (for entity <-
          collection.insertDocument[Stored, CrawlingExecution](execution, DocumentCreateOptions().returnNew(true))
      yield entity.getNew()).retry(collectionManager.sakePolicy)

    override def searchIntersection(
        dataCentreId: String,
        starting: ZonedDateTime,
        ending: ZonedDateTime
    ): Task[Seq[CrawlingExecution]] =
      collection.database
        .query[Stored, CrawlingExecution](
          IntersectionQuery,
          Map(
            "@collection"  -> collection.name,
            "dataCentreId" -> dataCentreId,
            "starting"     -> starting,
            "ending"       -> ending
          )
        )
        .runCollect
        .retry(collectionManager.sakePolicy)

    private def collection = collectionManager.collection

    override def updateCounting(execution: CrawlingExecution): Task[CrawlingExecution] =
      (for entity <- collection.updateDocument[Stored, UpdateCounting, CrawlingExecution](
                       execution.id,
                       execution,
                       DocumentUpdateOptions().returnNew(true)
                     )
      yield entity.getNew()).retry(collectionManager.sakePolicy)

    override def updateState(execution: CrawlingExecution): Task[CrawlingExecution] =
      (for entity <- collection.updateDocument[Stored, UpdateState, CrawlingExecution](
                       execution.id,
                       execution,
                       DocumentUpdateOptions().returnNew(true)
                     )
      yield entity.getNew()).retry(collectionManager.sakePolicy)

    private given FarangoTransformer[CrawlingExecution, UpdateCounting] with
      override def transform(execution: CrawlingExecution): UpdateCounting = UpdateCounting(
        updatedAt = zonedDateTimeService.now(),
        detected = execution.detected
      )

    private given FarangoTransformer[CrawlingExecution, UpdateState] with
      override def transform(execution: CrawlingExecution): UpdateState = UpdateState(
        updatedAt = zonedDateTimeService.now(),
        state = execution.state.ordinal
      )
