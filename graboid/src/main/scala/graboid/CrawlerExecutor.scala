package graboid

import _root_.quakeml.QuakeMLDetectedEvent
import com.softwaremill.macwire.wire
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import scala.language.experimental
import zio.Cause
import zio.Clock
import zio.Task
import zio.UIO
import zio.ZIO
import zio.stream.UStream
import zio.stream.ZStream
import zio.stream.ZStreamAspect

trait CrawlerExecutor:

  def run(): Task[CrawlingReport]

  def runPublisher(publisherKey: String): Task[CrawlingReport]

  def removeExecutions(publisherKey: String): Task[Long]

object CrawlerExecutor:

  def apply(
      repository: CrawlerExecutionRepository,
      scheduler: CrawlerScheduler,
      publisherManager: PublisherManager,
      eventManager: EventManager,
      crawlerFactory: CrawlerFactory
  ): CrawlerExecutor =
    wire[Impl]

  private type State = (Long, Long) // (time in milliseconds, count)

  private case class CrawlerTask(crawler: Crawler, execution: CrawlerExecution, publisher: Publisher)

  private case class CrawlerTaskReport(execution: CrawlerExecution, status: Throwable | Status)

  private case class Status(events: Long, magnitudes: Long, origins: Long, last: Option[ZonedDateTime]):

    def count(event: QuakeMLDetectedEvent): Status =
      copy(events = events + 1)

  private class Impl(
      repository: CrawlerExecutionRepository,
      scheduler: CrawlerScheduler,
      publisherManager: PublisherManager,
      eventManager: EventManager,
      crawlerFactory: CrawlerFactory
  ) extends CrawlerExecutor:

    def processors = math.max(Runtime.getRuntime().availableProcessors() / 2, 1)

    private val ttu            = 1000L * 30L
    private val maxCountUpdate = 50

    private def computeNextTTU(now: Long) = now + ttu // Time To Update

    def run(): Task[CrawlingReport] =
      for
        offset <- Clock.currentDateTime
        now     = offset
                    .toZonedDateTime()
                    .truncatedTo(ChronoUnit.SECONDS)

        _      <- ZIO.logInfo(s"Running Crawler Executor for $now.")
        stream <- publisherManager.getActives()
        report <- runStream(stream, now)
      yield report

    override def runPublisher(publisherKey: String): Task[CrawlingReport] =
      for
        offset <- Clock.currentDateTime
        now     = offset.toZonedDateTime()
        option <- publisherManager.get(publisherKey)
        report <- option match
                    case Some(publisher) => runStream(ZStream.succeed(publisher), now)
                    case None            => ZIO.logInfo(s"There is publisher=$publisherKey.") as CrawlingReport(0L, 0L, 0L, 0L)
      yield report

    private def runStream(stream: ZStream[Any, Throwable, Publisher], now: ZonedDateTime): Task[CrawlingReport] =
      stream
        .flatMapPar(processors)(createTasks(now))
        .mapZIOPar(processors)(crawlerTask =>
          for
            now    <- Clock.currentDateTime
            result <- executeTask(crawlerTask, now.toZonedDateTime())
          yield result
        )
        .runFold(CrawlingReport(0L, 0L, 0L, 0L))(updateCrawlerReport)

    override def removeExecutions(publisherKey: String): Task[Long] =
      for
        stream <- repository.removeWithPublisherKey(publisherKey)
        count  <- stream.runCount
      yield count

    def createTasks(now: ZonedDateTime)(publisher: Publisher): UStream[CrawlerTask] =
      val iteratorEffect = for
        searchLastResult <- repository.searchLast(publisher)
        iterator         <- searchLastResult match
                              case Some(last) => scheduler.computeSchedule(publisher, last, now)
                              case None       => scheduler.computeSchedule(publisher, now)
      yield iterator

      ZStream
        .fromIteratorZIO(iteratorEffect)
        .mapZIO(createTask(publisher))
        .collectSome
        .catchAll(handleCreateTasks(publisher)) @@ ZStreamAspect
        .annotated(
          ("publisherKey", publisher.key),
          ("publisherName", publisher.name)
        )

    def createTask(publisher: Publisher)(execution: CrawlerExecution): UIO[Option[CrawlerTask]] =
      val effect =
        for
          crawler <- crawlerFactory(publisher, execution)
          _       <- ZIO.logInfo("A new task was created.")
        yield Some(CrawlerTask(crawler, execution, publisher))

      effect.catchAll(handleCreateTask(publisher, execution))

    def executeTask(task: CrawlerTask, now: ZonedDateTime): UIO[CrawlerTaskReport] =
      val CrawlerTask(crawler, original, publisher) = task

      val nextTTU   = computeNextTTU(now.getLong(ChronoField.INSTANT_SECONDS))
      val execution = original.copy(
        status = Some(CrawlerExecution.Status.Running),
        executionStarted = Some(now),
        expectedStop = Some(now.`with`(ChronoField.INSTANT_SECONDS, nextTTU / 1000L)),
        executionStopped = None,
        message = Some("Working")
      )

      (for
        stream <- crawler.crawl(TimeWindow(beginning = original.beginning, ending = original.ending))
        _      <- repository.add(execution)
        report <- stream
                    .mapZIO(eventManager.register(_, publisher, execution))
                    .mapAccumZIO((nextTTU, 0L))(accumUpdateCrawlerExecution(publisher, execution))
                    .runFoldZIO(CrawlerTaskReport(execution, Status(0L, 0L, 0L, None)))(summarise)
      yield report)
        .foldZIO(failedExecution(publisher, execution), succeedExecution(publisher, execution))

    private def accumUpdateCrawlerExecution(publisher: Publisher, execution: CrawlerExecution)(
        state: State,
        event: QuakeMLDetectedEvent
    ): Task[(State, QuakeMLDetectedEvent)] =
      val (ttu, count) = state

      def computeNewState(now: Long): UIO[(State, QuakeMLDetectedEvent)] =
        if now >= ttu || count >= maxCountUpdate then
          val nextTTU = computeNextTTU(now)
          updateCrawlerExecution(publisher, execution, nextTTU) as ((nextTTU, 0L), event)
        else ZIO.succeed(((ttu, count + 1), event))

      for
        now      <- Clock.currentTime(ChronoUnit.SECONDS)
        newState <- computeNewState(now)
      yield newState

    private def updateCrawlerExecution(
        publisher: Publisher,
        execution: CrawlerExecution,
        nextTTU: Long
    ): UIO[CrawlerExecution] =
      for
        clock           <- Clock.javaClock
        updatedExecution = execution.copy(
                             status = Some(CrawlerExecution.Status.Running),
                             expectedStop =
                               Some(ZonedDateTime.ofInstant(Instant.ofEpochSecond(nextTTU), clock.getZone())),
                             executionStopped = None,
                             message = Some("Working.")
                           )
        _               <-
          repository
            .update(updatedExecution)
            .catchAll(cause =>
              ZIO.logWarningCause(
                s"It was impossible to update a running crawler-execution=${execution.key} from publisher=${publisher.key}.",
                Cause.die(cause)
              )
            )
      yield execution

    private def failedExecution(publisher: Publisher, execution: CrawlerExecution)(
        cause: Throwable
    ): UIO[CrawlerTaskReport] =
      for
        now             <- Clock.currentDateTime
        updatedExecution = execution.copy(
                             status = Some(CrawlerExecution.Status.Failed),
                             expectedStop = None,
                             executionStopped = Some(now.toZonedDateTime()),
                             message = Some("Failed: " + cause.getMessage())
                           )
        _               <-
          repository
            .update(updatedExecution)
            .catchAll(cause =>
              ZIO.logWarningCause(
                s"It was impossible to update a failed crawler-execution=${execution.key} from publisher=${publisher.key}.",
                Cause.die(cause)
              )
            )
      yield CrawlerTaskReport(updatedExecution, cause)

    private def succeedExecution(publisher: Publisher, execution: CrawlerExecution)(
        report: CrawlerTaskReport
    ): UIO[CrawlerTaskReport] =
      for
        now             <- Clock.currentDateTime
        updatedExecution = execution
                             .copy(
                               status = Some(CrawlerExecution.Status.Completed),
                               executionStopped = Some(now.toZonedDateTime()),
                               expectedStop = None,
                               message = Some("Ok!")
                             )
        _               <-
          repository
            .update(updatedExecution)
            .catchAll(cause =>
              ZIO.logWarningCause(
                s"It was impossible to update a succeed crawler-execution=${execution.key} from publisher=${publisher.key}.",
                Cause.die(cause)
              )
            )
      yield report

    def summarise(report: CrawlerTaskReport, event: QuakeMLDetectedEvent): UIO[CrawlerTaskReport] =
      report.status match
        case status: Status   =>
          ZIO.succeed(report.copy(status = status.count(event)))
        case error: Throwable =>
          ZIO.succeed(report) <* ZIO.logWarningCause("Unexpected error!", Cause.die(error))

    def updateCrawlerReport(report: CrawlingReport, taskReport: CrawlerTaskReport): CrawlingReport =
      taskReport.status match
        case Status(events, magnitudes, origins, _) =>
          report.copy(
            events = report.events + events,
            magnitudes = report.magnitudes + magnitudes,
            origins = report.origins + origins
          )

        case _ =>
          report.copy(failures = report.failures + 1)

    def handleCreateTasks(publisher: Publisher)(cause: Throwable): UStream[CrawlerTask] =
      ZStream.fromZIO(
        ZIO.logWarningCause("It was impossible to create CrawlerTasks.", Cause.die(cause))
      ) *> ZStream.empty

    def handleCreateTask(publisher: Publisher, execution: CrawlerExecution)(
        cause: Throwable
    ): UIO[Option[CrawlerTask]] =
      ZIO.logWarningCause("It was impossible to create a CrawlerTask.", Cause.die(cause)) as None
