package graboid.crawling

import com.softwaremill.macwire.wire
import graboid.CrawlingExecution
import graboid.DataCentre
import graboid.GraboidException
import graboid.GraboidException.CrawlingException
import graboid.context.ExecutionContext
import graboid.context.Owner
import graboid.manager.DataCentreManager
import graboid.repository.CrawlingExecutionRepository
import graboid.time.ZonedDateTimeService
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import zio.Cause
import zio.Chunk
import zio.Exit
import zio.Ref
import zio.ZIO
import zio.http.Client
import zio.kafka.producer.Producer
import zio.stream.ZStream
import zio.stream.ZStreamAspect

trait CrawlingExecutor:

  def execute(query: EventCrawlingQuery)(using
      ExecutionContext
  ): ZStream[Client & Producer, Throwable, EventCrawler.FoundEvent]

  def execute(
      dataCentre: DataCentre,
      query: EventCrawlingQuery
  )(using ExecutionContext): ZStream[Client & Producer, Throwable, EventCrawler.FoundEvent]

object CrawlingExecutor:

  def apply(
      repository: CrawlingExecutionRepository,
      dataCentreManager: DataCentreManager,
      eventCrawlerFactory: EventCrawler.Factory,
      keyGenerator: KeyGenerator,
      zonedDateTimeService: ZonedDateTimeService
  ): CrawlingExecutor =
    wire[Impl]

  private class Impl(
      repository: CrawlingExecutionRepository,
      dataCentreManager: DataCentreManager,
      eventCrawlerFactory: EventCrawler.Factory,
      keyGenerator: KeyGenerator,
      zonedDateTimeService: ZonedDateTimeService
  ) extends CrawlingExecutor:

    override def execute(
        query: EventCrawlingQuery
    )(using ExecutionContext): ZStream[Client & Producer, Throwable, EventCrawler.FoundEvent] =
      ZStream.logInfo("Executing Event Crawling for all DataCentres.") *>
        dataCentreManager.all
          .flatMapPar(parallelism)(dataCentre =>
            execute(dataCentre, query)
              .catchAll(handleEventCrawlingError)
          )

    private def parallelism = math.max(Runtime.getRuntime.availableProcessors() / 2, 1)

    override def execute(
        dataCentre: DataCentre,
        query: EventCrawlingQuery
    )(using ExecutionContext): ZStream[Client & Producer, Throwable, EventCrawler.FoundEvent] =
      ZStream
        .fromZIO {
          summon[ExecutionContext].owner match
            case Owner.Scheduler =>
              repository.searchIntersection(dataCentre.id, query.starting, query.ending)
            case _               =>
              ZIO.logDebug("It is a non scheduled crawling and will be executed.") as Nil
        }
        .flatMap { executions =>
          if executions.isEmpty then
            execute(
              dataCentre,
              query,
              CrawlingExecution(
                id = keyGenerator.generate(KeyLength.Medium),
                dataCentreId = dataCentre.id,
                createdAt = zonedDateTimeService.now(),
                updatedAt = None,
                starting = query.starting,
                ending = query.ending,
                detected = 0L,
                state = CrawlingExecution.State.Starting
              )
            )
          else
            val intersections = (for e <- executions yield s"(${e.starting}, ${e.ending})").mkString(", ")
            ZStream.fail(
              GraboidException.CrawlingException(
                s"There are some CrawlingExecution which already covered this period: $intersections."
              )
            )
        } @@ annotated(dataCentre)

    private def annotated(dataCentre: DataCentre) = ZStreamAspect.annotated(
      "crawlingExecutor.dataCentreId" -> dataCentre.id
    )

    private def execute(
        dataCentre: DataCentre,
        query: EventCrawlingQuery,
        execution: CrawlingExecution
    ): ZStream[Client & Producer, Nothing, EventCrawler.FoundEvent] =
      ZStream
        .fromZIO(repository.insert(execution))
        .mapZIO(stored => eventCrawlerFactory(dataCentre).zip(Ref.make(stored)))
        .catchAll { cause =>
          ZStream.fromZIO(
            ZIO.logErrorCause("It was impossible to create an EventCrawler!", Cause.die(cause))
          ) *> ZStream.empty
        }
        .flatMap { (crawler, ref) =>

          def handleChunk(chunk: Chunk[EventCrawler.FoundEvent]) =
            ZStream.fromZIO {
              for
                execution <- ref.get
                updated   <- repository.updateCounting(execution.copy(detected = execution.detected + chunk.size))
                _         <- ref.set(updated)
              yield chunk
            }.flattenChunks

          def finalize(exit: Exit[Throwable, Any]) =
            for
              execution <- ref.get
              _         <- exit match
                             case Exit.Success(_)     =>
                               repository
                                 .updateState(execution.copy(state = CrawlingExecution.State.Completed))
                                 .ignoreLogged *>
                                 ZIO.logInfo(s"EventCrawling has been finished: succeed=${execution.detected}.")
                             case Exit.Failure(cause) =>
                               repository
                                 .updateState(execution.copy(state = CrawlingExecution.State.Failed))
                                 .ignoreLogged *>
                                 ZIO.logErrorCause(s"EventCrawling has failed: succeed=${execution.detected}.", cause)
            yield execution

          crawler(query)
            .grouped(64)
            .flatMap(handleChunk)
            .ensuringWith(finalize)
            .catchAll(_ => ZStream.empty)
        } @@ ZStreamAspect.annotated(
        "crawlingExecutor.dataCentreId" -> dataCentre.id,
        "crawlingExecutor.starting"     -> query.starting.toString,
        "crawlingExecutor.ending"       -> query.ending.toString
      )

    private def handleEventCrawlingError(cause: Throwable): ZStream[Any, Nothing, Nothing] =
      ZStream.fromZIO(ZIO.logErrorCause("It was impossible to crawl events.", Cause.die(cause))) *> ZStream.empty
