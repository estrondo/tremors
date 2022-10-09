package tremors.graboid

import tremors.graboid.quakeml.model.Event
import zio.Cause
import zio.UIO
import zio.URIO
import zio.ZIO
import zio.stream.ZStream

import java.time.Duration
import java.time.ZonedDateTime

import CrawlerSupervisor.*

object CrawlerSupervisor:

  /** @param crawlerName
    * @param windowDuration
    *   in days.
    */
  case class Config(
      crawlerName: String,
      windowDuration: Int,
      beginning: ZonedDateTime
  )

  def apply(config: Config, crawler: Crawler): CrawlerSupervisor =
    CrawlerSupervisorImpl(config, crawler)

trait CrawlerSupervisor:

  def start(): URIO[TimelineManager, Crawler.Stream]

private class CrawlerSupervisorImpl(config: Config, crawler: Crawler) extends CrawlerSupervisor:
  override def start(): URIO[TimelineManager, Crawler.Stream] =
    val result = for
      repository <- ZIO.service[TimelineManager]
      window     <- repository.nextWindow(config.crawlerName)
      stream     <- visit(window, repository)
    yield stream

    result.catchAll(error =>
      ZIO.succeed(
        ZStream.fail(GraboidException.Unexpected("Error during searching for events!", error))
      )
    )

  private def visit(
      interval: TimelineManager.Window,
      repository: TimelineManager
  ): UIO[Crawler.Stream] =
    for
      _      <- ZIO.logInfo(s"Searching for events in $interval.")
      stream <- crawler
                  .crawl(interval)
                  .catchAll(error =>
                    ZIO.succeed(ZStream.fail(error)) <& ZIO
                      .logErrorCause("Failed to crawl!", Cause.die(error))
                  )
    yield handle(stream, interval, repository)

  private def handle(
      stream: Crawler.Stream,
      window: TimelineManager.Window,
      repository: TimelineManager
  ): Crawler.Stream =
    stream.filterZIO {
      case event: Event =>
        if window.contains(event.creationInfo) then ZIO.succeed(true)
        else ZIO.succeed(false) <& ZIO.logDebug(s"Event ${event.publicID} was ignored.")

      case _ => ZIO.succeed(false)
    }

  private def handleInfo(window: TimelineManager.Window, manager: TimelineManager)(
      count: Int,
      info: Crawler.Info
  ): UIO[(Int, Crawler.Info)] = ???
