package graboid.crawling

import com.softwaremill.macwire.wire
import graboid.CrawlingExecution
import graboid.DataCentre
import graboid.repository.CrawlingExecutionRepository
import graboid.time.ZonedDateTimeService
import scala.util.Try
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.Exit
import zio.RIO
import zio.ZIO
import zio.ZIOAspect
import zio.http.Client
import zio.kafka.producer.Producer

trait CrawlingExecutor:

  def execute(
      dataCentre: DataCentre,
      query: EventCrawlingQuery
  ): RIO[ZonedDateTimeService & Client & Producer, CrawlingExecution]

object CrawlingExecutor:

  def apply(
      repository: CrawlingExecutionRepository,
      eventCrawlerFactory: EventCrawler.Factory,
      keyGenerator: KeyGenerator
  ): CrawlingExecutor =
    wire[Impl]

  private class Impl(
      repository: CrawlingExecutionRepository,
      eventCrawlerFactory: EventCrawler.Factory,
      keyGenerator: KeyGenerator
  ) extends CrawlingExecutor:

    override def execute(
        dataCentre: DataCentre,
        query: EventCrawlingQuery
    ): RIO[ZonedDateTimeService & Client & Producer, CrawlingExecution] =
      for
        id        <- ZIO.fromTry(Try(keyGenerator.generate(KeyLength.Medium)))
        createdAt <- ZonedDateTimeService.now()
        execution  = CrawlingExecution(
                       id = id,
                       schedulingId = query.schedulingId,
                       createdAt = createdAt,
                       updatedAt = None,
                       succeed = 0L,
                       failed = 0L,
                       state = CrawlingExecution.State.Starting
                     )

        _      <- repository.insert(execution)
        result <- execute(execution, dataCentre, query) @@ ZIOAspect.annotated(
                    "crawlingExecutor.dataCentreId" -> dataCentre.id,
                    "crawlingExecutor.executionId"  -> id
                  )
      yield result

    private def execute(
        execution: CrawlingExecution,
        dataCentre: DataCentre,
        query: EventCrawlingQuery
    ): RIO[Client & ZonedDateTimeService & Producer, CrawlingExecution] =
      for
        crawler     <- eventCrawlerFactory(dataCentre)
        timeService <- ZIO.service[ZonedDateTimeService]
        exit        <- crawler(query)
                         .grouped(64)
                         .mapAccumZIO(execution.copy(state = CrawlingExecution.State.Running)) { (execution, chunk) =>
                           val newExecution = chunk
                             .foldLeft(execution) {
                               case (execution, _: EventCrawler.Success) => execution.copy(succeed = execution.succeed + 1)
                               case (execution, _: EventCrawler.Failed)  => execution.copy(failed = execution.failed + 1)
                             }
                             .copy(updatedAt = Some(timeService.now()))

                           for _ <- repository.updateCounting(newExecution) yield (newExecution, newExecution)
                         }
                         .runLast
                         .exit

        result <- exit match
                    case Exit.Success(Some(execution)) =>
                      repository.updateState(
                        execution.copy(state = CrawlingExecution.State.Completed)
                      ) <* ZIO.logInfo(
                        s"Execution has been finished with succeed=${execution.succeed}, failed=${execution.failed}."
                      )

                    case Exit.Success(None)  =>
                      repository.updateState(
                        execution.copy(updatedAt = Some(timeService.now()), state = CrawlingExecution.State.Completed)
                      ) <* ZIO.logInfo("Execution has been finished with not result!")
                    case Exit.Failure(cause) =>
                      repository.updateState(
                        execution.copy(updatedAt = Some(timeService.now()), state = CrawlingExecution.State.Failed)
                      ) <* ZIO.logErrorCause("Execution has been finished as a failure!", cause)
      yield result
