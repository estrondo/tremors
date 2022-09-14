package tremors.graboid.timeline

import tremors.graboid.Crawler
import tremors.graboid.Crawler.contains
import tremors.graboid.repository.TimelineRepository
import zio.*

import CrawlerSupervisor.*
import zio.stream.ZStream
import java.sql.Time
import tremors.graboid.quakeml.model.Event

object CrawlerSupervisor:

  case class Config(
      crawlerName: String,
      intervalLong: Int
  )

  def apply(config: Config, crawler: Crawler): CrawlerSupervisor =
    CrawlerSupervisorImpl(config, crawler)

trait CrawlerSupervisor:

  def start(): URIO[TimelineRepository, Crawler.Stream]

private class CrawlerSupervisorImpl(config: Config, crawler: Crawler) extends CrawlerSupervisor:
  override def start(): URIO[TimelineRepository, Crawler.Stream] =
    for
      repository      <- ZIO.service[TimelineRepository]
      optinalInterval <- repository.nextInterval(config.crawlerName, config.intervalLong)
      stream          <- optinalInterval match
                           case Some(interval) => visit(interval, repository)
                           case None           => ZIO.succeed(ZStream.empty)
    yield stream

  private def visit(
      interval: Crawler.Interval,
      repository: TimelineRepository
  ): UIO[Crawler.Stream] =
    for
      _      <- ZIO.logInfo(s"Searching for events beetwen $interval.")
      stream <- crawler
                  .crawl(interval)
                  .catchAll(error =>
                    ZIO.succeed(ZStream.fail(error)) <& ZIO
                      .logErrorCause("Failed to crawl!", Cause.die(error))
                  )
    yield handle(stream, interval, repository)

  private def handle(
      stream: Crawler.Stream,
      interval: Crawler.Interval,
      repository: TimelineRepository
  ): Crawler.Stream =
    stream.filterZIO {
      case event: Event =>
        if interval.contains(event.creationInfo) then ZIO.succeed(true)
        else ZIO.succeed(false) <& ZIO.logDebug(s"Event ${event.publicID} was ignored.")

      case _ => ZIO.succeed(false)
    }

  private def handleInfo(interval: Crawler.Interval, repository: TimelineRepository)(
      count: Int,
      info: Crawler.Info
  ): UIO[(Int, Crawler.Info)] = ???
