package graboid

import com.softwaremill.macwire.wire
import zio.Task
import zio.stream.ZStream
import zio.stream.UStream

import java.awt.Taskbar
import zio.ZIO
import zio.UIO
import zio.RIO
import java.time.ZonedDateTime
import zio.stream.ZStreamAspect
import zio.Cause
import _root_.quakeml.Event
import _root_.quakeml.Origin
import _root_.quakeml.Magnitude
import zio.Clock

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
    wire[CrawlerExecutorImpl]

  private case class CrawlerTask(crawler: Crawler, execution: CrawlerExecution, publisher: Publisher)

  private case class CrawlerTaskReport(execution: CrawlerExecution, status: Throwable | Status)

  private case class Status(events: Long, magnitudes: Long, origins: Long, last: Option[ZonedDateTime]):

    def count(info: Crawler.Info): Status =
      info match
        case _: Event     => copy(events = events + 1)
        case _: Origin    => copy(origins = origins + 1)
        case _: Magnitude => copy(magnitudes = magnitudes + 1)

  private class CrawlerExecutorImpl(
      repository: CrawlerExecutionRepository,
      scheduler: CrawlerScheduler,
      publisherManager: PublisherManager,
      eventManager: EventManager,
      crawlerFactory: CrawlerFactory
  ) extends CrawlerExecutor:

    def processors = math.max(Runtime.getRuntime().availableProcessors() / 2, 1)

    def run(): Task[CrawlingReport] =
      for
        now    <- Clock.currentDateTime.map(_.toZonedDateTime())
        stream <- publisherManager.getActives()
        report <- stream
                    .flatMapPar(processors)(createTasks(now))
                    .mapZIOPar(processors)(executeTask)
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

    def executeTask(task: CrawlerTask): UIO[CrawlerTaskReport] =
      val CrawlerTask(crawler, execution, publisher) = task
      (for
        stream <- crawler.crawl(TimeWindow(beginning = execution.beginning, ending = execution.ending))
        report <- stream
                    .mapZIO(eventManager.register(_, publisher, execution))
                    .runFoldZIO(CrawlerTaskReport(execution, Status(0L, 0L, 0L, None)))(summarise)
      yield report).catchAll(handleExecuteTask(task))

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

    def handleExecuteTask(task: CrawlerTask)(cause: Throwable): UIO[CrawlerTaskReport] =
      ZIO.logWarningCause("It was impossible to execute a Task.", Cause.die(cause)) as (
        CrawlerTaskReport(
          task.execution,
          cause
        )
      )
