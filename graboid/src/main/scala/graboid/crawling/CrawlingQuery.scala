package graboid.crawling

import graboid.http.UpdateQueryParam
import java.time.ZonedDateTime
import scala.collection.immutable.HashMap
import scala.util.Try

case class CrawlingQuery(
    starting: ZonedDateTime,
    ending: ZonedDateTime,
    maxMagnitude: Option[Double],
    minMagnitude: Option[Double]
)

object CrawlingQuery:

  given UpdateQueryParam[CrawlingQuery] with

    override def getParams(value: CrawlingQuery): Try[Map[String, String]] =
      Try {
        var result = HashMap(
          "starttime" -> value.starting.toString,
          "endtime"   -> value.ending.toString
        )

        def addDouble(opt: Option[Double], name: String): Unit =
          result = opt.foldLeft(result) { (map, value) => map + (name -> value.toString) }

        addDouble(value.maxMagnitude, "maxmagnitude")
        addDouble(value.minMagnitude, "minmagnitude")

        result
      }
