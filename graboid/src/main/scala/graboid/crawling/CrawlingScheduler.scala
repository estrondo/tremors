package graboid.crawling

import com.softwaremill.macwire.wire
import graboid.crawling.CrawlingScheduler.EventConfig
import graboid.time.ZonedDateTimeService
import java.time.Duration
import java.time.ZonedDateTime
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

  def apply(dataStore: DataStore, crawlingExecutor: CrawlingExecutor): Task[CrawlingScheduler] = ZIO.succeed(wire[Impl])

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

      def handleTimeRangeError(cause: Throwable) =
        ZStream.fromZIO(
          ZIO.logErrorCause(
            "An unexpected error has occurred while the previous time reference had been searched!",
            Cause.die(cause)
          )
        ) *> ZStream.empty

      def handleTimeRange(starting: ZonedDateTime, ending: ZonedDateTime) =
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
          .execute(eventQuery)
          .map(_.event)
          .catchAll { cause =>
            ZStream.fromZIO(
              ZIO.logErrorCause("It was impossible to execute a crawling!", Cause.die(cause))
            ) *> ZStream.empty
          }
          .ensuring(updateDataStore(ending, EventTimeMark))

      ZStream
        .fromZIO(defineNextTimeRange(config))
        .catchAll(handleTimeRangeError)
        .flatMap {
          case Some((starting, ending)) =>
            ZStream.logInfo(s"Event will be searched between ($starting,$ending).") *> handleTimeRange(starting, ending)
          case None                     =>
            ZStream.logInfo("No Events will be searched!") *> ZStream.empty
        }

    private def updateDataStore(zonedDateTime: ZonedDateTime, key: String) =
      ZIO.logInfo(s"Updating dataStore[$key] to $zonedDateTime.") <* dataStore.put(key, zonedDateTime).catchAll {
        cause => ZIO.logErrorCause(s"It was impossible to update DataStore[$key]!", Cause.die(cause))
      }

    private def defineNextTimeRange(config: EventConfig) =
      for
        now      <- ZIO.serviceWith[ZonedDateTimeService](_.now())
        previous <- dataStore.get[ZonedDateTime](EventTimeMark)
      yield previous match
        case Some(previous) if previous.compareTo(now) < 0 => Some(previous -> now)
        case None                                          => Some(now.minusSeconds(config.interval.toSeconds) -> now)
        case _                                             => None
