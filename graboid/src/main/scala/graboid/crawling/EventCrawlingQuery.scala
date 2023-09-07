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
    queries: Seq[Query]
)

object EventCrawlingQuery:

  case class Query(
      magnitudeType: Option[String],
      eventType: Option[String],
      min: Option[Double],
      max: Option[Double]
  )

  given UpdateQueryParam[EventCrawlingQuery] with

    override def getParams(query: EventCrawlingQuery): Task[Seq[Iterable[(String, String)]]] =
      ZIO.attempt {
        val template = HashMap(
          "starttime" -> query.starting.toString,
          "endtime"   -> query.ending.toString
        )

        inline def add(map: HashMap[String, String], opt: Option[Any], key: String): HashMap[String, String] =
          opt match
            case Some(value) => map + (key -> value.toString)
            case _           => map

        for magnitude <- query.queries yield
          var result = add(template, magnitude.magnitudeType, "magnitudetype")
          result = add(result, magnitude.min, "minmagnitude")
          result = add(result, magnitude.max, "maxmagnitude")
          add(result, magnitude.eventType, "eventtype")
      }
