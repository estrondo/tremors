package farango.data

import io.github.arainko.ducktape.Transformer

import java.lang.{Long => JLong}
import java.net.URI
import java.net.URL
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

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

  given ZoneDateTimeToJLong: Transformer[ZonedDateTime, JLong] =
    _.getLong(ChronoField.INSTANT_SECONDS)

  given JLongToZoneDateTime: Transformer[JLong, ZonedDateTime] = longValue =>
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(longValue), ZoneId)

  given [A, B](using Transformer[A, B], Null <:< B): Transformer[Option[A], B] =
    _.map(summon[Transformer[A, B]].transform).orNull

  given [A, B](using Transformer[A, B]): Transformer[A, Option[B]] = value =>
    if value != null then Some(summon[Transformer[A, B]].transform(value))
    else None

  given [A, B](using conversion: Conversion[A, B]): Transformer[A, B] =
    conversion.apply(_)
