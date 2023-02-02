package graboid

import _root_.quakeml.Event
import _root_.quakeml.Magnitude
import _root_.quakeml.Origin
import com.softwaremill.macwire.wire
import zio.Cause
import zio.Clock
import zio.RIO
import zio.Task
import zio.UIO
import zio.ZIO
import zio.stream.UStream
import zio.stream.ZStream
import zio.stream.ZStreamAspect

import java.awt.Taskbar
import java.time.ZonedDateTime
import graboid.GraboidException.CrawlerException
import java.net.URI
import zio.RIO
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoField
import scala.language.experimental
import java.time.LocalDateTime
import java.time.Instant

trait CrawlerExecutor:

  def run(): Task[CrawlingReport]

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

    def count(info: Crawler.Info): Status =
      info match
        case _: Event     => copy(events = events + 1)
        case _: Origin    => copy(origins = origins + 1)
        case _: Magnitude => copy(magnitudes = magnitudes + 1)

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
        now     = offset.toZonedDateTime()

        _      <- ZIO.logInfo(s"Running Crawler Executor for $now.")
        stream <- publisherManager.getActives()
        report <- stream
                    .flatMapPar(processors)(createTasks(now))
                    .mapZIOPar(processors)(crawlerTask =>
                      for
                        now    <- Clock.currentDateTime
                        result <- executeTask(crawlerTask, now.toZonedDateTime())
                      yield result
                    )
                    .runFold(CrawlingReport(0L, 0L, 0L, 0L))(updateCrawlerReport)
      yield report

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
        info: Crawler.Info
    ): Task[(State, Crawler.Info)] =
      val (ttu, count) = state

      def computeNewState(now: Long): UIO[(State, Crawler.Info)] =
        if now >= ttu || count >= maxCountUpdate then
          val nextTTU = computeNextTTU(now)
          updateCrawlerExecution(publisher, execution, nextTTU) as ((nextTTU, 0L), info)
        else ZIO.succeed(((ttu, count + 1), info))

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

    def summarise(report: CrawlerTaskReport, info: Crawler.Info): UIO[CrawlerTaskReport] =
      report.status match
        case status: Status   =>
          ZIO.succeed(report.copy(status = status.count(info)))
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
