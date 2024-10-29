package graboid.crawling

import graboid.crawling.EventCrawlingQuery.Query
import graboid.http.UpdateQueryParam
import java.time.Duration
import java.time.ZonedDateTime
import scala.collection.immutable.HashMap
import zio.Task
import zio.ZIO

case class EventCrawlingQuery(
    starting: ZonedDateTime,
    ending: ZonedDateTime,
    timeWindow: Duration,
    queries: Seq[Query],
)

object EventCrawlingQuery:

  case class Query(
      magnitudeType: Option[String],
      eventType: Option[String],
      min: Option[Double],
      max: Option[Double],
  )

  given UpdateQueryParam[EventCrawlingQuery] with

    override def getParams(query: EventCrawlingQuery): Task[Iterable[Iterable[(String, String)]]] =
      ZIO.attempt {

        Iterable
          .unfold(
            query.starting -> query.starting.plus(query.timeWindow),
          ) { (starting, ending) =>
            for (currentStarting, currentEnding) <- filterTimeWindow(starting, ending, query.ending)
            yield
              val template = HashMap(
                "starttime" -> currentStarting.toString,
                "endtime"   -> currentEnding.toString,
              )

              val params = for query <- query.queries yield
                var current = add(template, query.magnitudeType, "magnitudetype")
                current = add(current, query.min, "minmagnitude")
                current = add(current, query.max, "maxmagnitude")
                add(current, query.eventType, "eventtype")

              params -> (currentEnding -> currentEnding.plus(query.timeWindow))
          }
          .flatten
      }

    private inline def add(map: HashMap[String, String], opt: Option[Any], key: String): HashMap[String, String] =
      opt match
        case Some(value) => map + (key -> value.toString)
        case _           => map

    private def filterTimeWindow(
        starting: ZonedDateTime,
        ending: ZonedDateTime,
        limit: ZonedDateTime,
    ): Option[(ZonedDateTime, ZonedDateTime)] =
      if starting.compareTo(limit) < 0 then
        if ending.compareTo(limit) <= 0 then Some(starting -> ending) else Some(starting -> limit)
      else None
