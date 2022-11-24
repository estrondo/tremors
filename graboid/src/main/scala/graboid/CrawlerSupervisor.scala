package graboid

import _root_.quakeml.Event
import cbor.quakeml.given
import com.softwaremill.macwire.*
import graboid.protocol.CrawlerDescriptor
import io.bullet.borer.Cbor
import io.bullet.borer.Codec
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.clients.producer.internals.ProducerMetadata
import zio.Cause
import zio.RIO
import zio.Task
import zio.TaskLayer
import zio.UIO
import zio.URIO
import zio.ZIO
import zio.kafka.producer.Producer
import zio.kafka.serde.*
import zio.stream.ZSink
import zio.stream.ZStream

import java.time.Duration
import java.time.ZonedDateTime
import scala.util.Failure
import scala.util.Success

import CrawlerSupervisor.*

trait CrawlerSupervisor:

  def run(): RIO[TimelineManager, Status]

object CrawlerSupervisor:

  val Topic = "tremors.detected-event"

  case class Config(crawlerName: String)

  case class Status(
      success: Long,
      fail: Long,
      skip: Long
  )

  def apply(
      producer: TaskLayer[Producer]
  )(descriptor: CrawlerDescriptor, crawler: Crawler): CrawlerSupervisor =
    CrawlerSupervisor(Config(descriptor.name), crawler, producer)

  def apply(config: Config, crawler: Crawler, producer: TaskLayer[Producer]): CrawlerSupervisor =
    wire[CrawlerSupervisorImpl]

private class CrawlerSupervisorImpl(config: Config, crawler: Crawler, producer: TaskLayer[Producer])
    extends CrawlerSupervisor:

  type E = (Option[String], Option[Throwable])

  override def run(): RIO[TimelineManager, Status] =
    for
      timelineManager <- ZIO.service[TimelineManager]
      window          <- timelineManager.nextWindow(config.crawlerName)
      stream          <-
        crawl(window, timelineManager)
          .mapError(
            GraboidException.CrawlerException(
              s"An error has ocurred when the crawler ${config.crawlerName} was crawling between ${window}!",
              _
            )
          )
      either          <- stream
                           .run(ZSink.foldLeftZIO(Status(success = 0L, fail = 0L, skip = 0L))(foldStream))
                           .either
      status          <- saveWindow(either, window, timelineManager)
    yield status

  private def crawl(
      window: TimelineManager.Window,
      timelineManager: TimelineManager
  ): Task[ZStream[Any, Throwable, E]] =
    for
      _      <- ZIO.logInfo(s"Searching for events in $window.")
      stream <- crawler.crawl(window)
    yield handle(stream, window, timelineManager).provideLayer(producer)

  private def foldStream(status: Status, e: E): UIO[Status] =
    e match
      case (Some(publicID), None) =>
        ZIO.succeed(status.copy(success = status.success + 1)) <& ZIO.logDebug(
          s"Info $publicID has been detected"
        )

      case (Some(publicID), Some(error)) =>
        ZIO.succeed(status.copy(fail = status.fail + 1)) <& ZIO.logErrorCause(
          s"Info $publicID has been failed",
          Cause.die(error)
        )

      case (None, None) =>
        ZIO.succeed(status.copy(skip = status.skip + 1)) <& ZIO.logDebug(
          "A unhandled info has been skiped"
        )

      case (None, Some(error)) =>
        ZIO.succeed(status.copy(skip = status.skip + 1)) <& ZIO.logErrorCause(
          "A unhandled info has thrown an exception",
          Cause.die(error)
        )

  private def handle(
      stream: Crawler.Stream,
      window: TimelineManager.Window,
      repository: TimelineManager
  ): ZStream[Producer, Throwable, E] =
    stream.collectZIO {
      case event: Event if window.contains(event.creationInfo) =>
        handleEvent(event)
    }

  private def handleEvent(
      event: Event
  ): ZIO[Producer, Throwable, E] =
    Cbor.encode(event).toByteArrayTry match
      case Success(byteArray) =>
        produce(event.publicID.uri, byteArray) as (Some(event.publicID.uri), None)

      case Failure(cause) =>
        ZIO.succeed((Some(event.publicID.uri), Some(cause)))

  private def produce(key: String, bytes: Array[Byte]): ZIO[Producer, Throwable, RecordMetadata] =
    Producer.produce(
      ProducerRecord(Topic, key, bytes),
      Serializer.string,
      Serializer.byteArray
    )

  private def saveWindow(
      either: Either[Throwable, Status],
      window: TimelineManager.Window,
      timelineManager: TimelineManager
  ): Task[Status] =
    either match
      case Right(status) =>
        for
          _ <- timelineManager.register(config.crawlerName, window)
          _ <-
            ZIO.logInfo(
              s"There have been detected ${status.success} successes, ${status.fail} fails and ${status.skip} skips"
            )
        yield status

      case Left(error) =>
        for _ <- ZIO.logErrorCause("An error has occurred", Cause.die(error))
        yield Status(0L, 0L, 0L)
