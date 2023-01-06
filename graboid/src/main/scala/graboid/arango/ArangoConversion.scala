package graboid.arango

import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoField
import java.time.ZonedDateTime
import java.time.Clock
import java.time.Instant
import java.time.Duration
import com.fasterxml.jackson.databind.util.Converter
import scala.Conversion
import java.lang.{Long => JLong}
import java.net.URL
import java.net.URI
import io.github.arainko.ducktape.Transformer
import graboid.Crawler
import javax.xml.crypto.dsig.Transform

object ArangoConversion:

  val ZoneId = Clock.systemUTC().getZone()

  given zonedDateTimeToLong: Transformer[ZonedDateTime, Long] =
    _.getLong(ChronoField.INSTANT_SECONDS)

  given Transformer[Long, ZonedDateTime] = longValue =>
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(longValue), ZoneId)

  given Transformer[Long, Duration] = Duration.ofSeconds(_)

  given Transformer[Duration, Long] = _.getSeconds()

  given Transformer[URL, String] = _.toExternalForm()

  given Transformer[String, URL] = URI.create(_).toURL()

  given Transformer[Crawler.Type, Int] = value => value.ordinal

  given Transformer[Int, Crawler.Type] = value => Crawler.Type.fromOrdinal(value)

  given ZoneDateTimeToJLong: Transformer[ZonedDateTime, JLong] =
    _.getLong(ChronoField.INSTANT_SECONDS)

  given JLongToZoneDateTime: Transformer[JLong, ZonedDateTime] = longValue =>
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(longValue), ZoneId)

  given [A, B >: Null](using Transformer[A, B]): Transformer[Option[A], B] =
    _.map(summon[Transformer[A, B]].transform(_)).orNull

  given [A >: Null, B](using Transformer[A, B]): Transformer[A, Option[B]] = value =>
    if value != null then Some(summon[Transformer[A, B]].transform(value))
    else None
