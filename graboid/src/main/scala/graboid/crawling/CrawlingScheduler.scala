package graboid.crawling

import graboid.context.ExecutionContext
import graboid.crawling.CrawlingScheduler.EventConfig
import graboid.time.ZonedDateTimeService
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import tremors.quakeml.Event
import tremors.zio.farango.DataStore
import zio.Cause
import zio.Task
import zio.ZIO
import zio.http.Client
import zio.kafka.producer.Producer
import zio.stream.ZStream

trait CrawlingScheduler:

  def start(config: EventConfig): ZStream[ZonedDateTimeService & Client & Producer, Nothing, Event]

object CrawlingScheduler:

  val EventTimeMark = "crawling.scheduler.eventTimeMark"

  def apply(dataStore: DataStore, crawlingExecutor: CrawlingExecutor): Task[CrawlingScheduler] =
    ZIO.succeed(Impl(dataStore, crawlingExecutor))

  case class EventConfig(
      interval: Duration,
      queryWindow: Duration,
      queries: Seq[EventQuery]
  )

  case class EventQuery(
      magnitudeType: Option[String],
      minMagnitude: Option[Double],
      maxMagnitude: Option[Double],
      eventType: Option[String]
  )

  private class Impl(dataStore: DataStore, crawlingExecutor: CrawlingExecutor) extends CrawlingScheduler:

    override def start(config: EventConfig): ZStream[ZonedDateTimeService & Client & Producer, Nothing, Event] =

      def handleTimeIntervalError(cause: Throwable) =
        ZStream.fromZIO(
          ZIO.logErrorCause(
            "An unexpected error has occurred while the previous time reference had been searched!",
            Cause.die(cause)
          )
        ) *> ZStream.empty

      def handleCrawlingError(cause: Throwable) =
        ZStream.fromZIO(
          ZIO.logErrorCause("It was impossible to execute a crawling!", Cause.die(cause))
        ) *> ZStream.empty

      def handleTimeInterval(starting: ZonedDateTime, ending: ZonedDateTime) =
        val eventQuery = EventCrawlingQuery(
          starting = starting,
          ending = ending,
          timeWindow = config.queryWindow,
          queries =
            for query <- config.queries
            yield EventCrawlingQuery.Query(
              magnitudeType = query.magnitudeType,
              eventType = query.eventType,
              min = query.minMagnitude,
              max = query.maxMagnitude
            )
        )

        crawlingExecutor
          .execute(eventQuery)(using ExecutionContext.scheduler())
          .map(_.event)
          .catchAll(handleCrawlingError)
          .ensuring(updateDataStore(ending, EventTimeMark))

      ZStream
        .fromZIO(searchNextTimeInterval(config, EventTimeMark))
        .catchAll(handleTimeIntervalError)
        .flatMap {
          case Some((starting, ending)) =>
            ZStream
              .logInfo(s"Events will be searched between ($starting, $ending).") *> handleTimeInterval(starting, ending)
          case None                     =>
            ZStream
              .logInfo("No Events will be searched!") *> ZStream.empty
        }

    private def updateDataStore(zonedDateTime: ZonedDateTime, key: String) =
      ZIO.logInfo(s"Updating dataStore[$key] to $zonedDateTime.") <* dataStore.put(key, zonedDateTime).catchAll {
        cause => ZIO.logErrorCause(s"It was impossible to update DataStore[$key]!", Cause.die(cause))
      }

    private def searchNextTimeInterval(config: EventConfig, mark: String) =
      for
        now      <- ZIO.serviceWith[ZonedDateTimeService](_.now().truncatedTo(ChronoUnit.MINUTES))
        previous <- dataStore.get[ZonedDateTime](mark)
      yield previous match
        case Some(previous) if previous.compareTo(now) < 0 => Some(previous -> now)
        case None                                          => Some(now.minusMinutes(config.interval.toMinutes) -> now)
        case _                                             => None
