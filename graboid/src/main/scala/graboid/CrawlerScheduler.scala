package graboid

import com.softwaremill.macwire.wire
import core.KeyGenerator
import core.KeyGenerator.KeyLength
import zio.Task
import zio.ZIO
import zio.stream.UStream

import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

trait CrawlerScheduler:

  def computeSchedule(
      publisher: Publisher,
      last: CrawlerExecution,
      reference: ZonedDateTime
  ): Task[Iterator[CrawlerExecution]]

  def computeSchedule(
      publisher: Publisher,
      reference: ZonedDateTime
  ): Task[Iterator[CrawlerExecution]]

object CrawlerScheduler:

  val UTCZoneId: ZoneId = ZoneOffset.UTC

  def apply(): CrawlerScheduler = wire[CrawlerSchedulerImpl]

  private def normalise(zonedDateTime: ZonedDateTime): ZonedDateTime =
    zonedDateTime.withZoneSameInstant(UTCZoneId)

  private class CrawlerSchedulerImpl extends CrawlerScheduler:

    def computeSchedule(publisher: Publisher, reference: ZonedDateTime): Task[Iterator[CrawlerExecution]] =
      computeSchedule(publisher, normalise(publisher.beginning), normalise(reference))

    def computeSchedule(
        publisher: Publisher,
        last: CrawlerExecution,
        reference: ZonedDateTime
    ): Task[Iterator[CrawlerExecution]] =
      computeSchedule(publisher, normalise(last.ending), normalise(reference))

    private def createExecution(
        publisher: Publisher,
        beginning: ZonedDateTime,
        ending: ZonedDateTime
    ): CrawlerExecution =
      CrawlerExecution(KeyGenerator.next(KeyLength.L24), publisher.key, beginning, ending)

    private def computeSchedule(
        publisher: Publisher,
        from: ZonedDateTime,
        to: ZonedDateTime
    ): Task[Iterator[CrawlerExecution]] = ZIO.fromEither {
      if from.compareTo(to) < 0 then Right(computeIterator(publisher, from, to))
      else if from.compareTo(to) == 0 then Right(Iterator.empty)
      else Left(GraboidException.IllegalState(s"Invalid: $from !< $to"))
    }

    private def computeIterator(
        publisher: Publisher,
        from: ZonedDateTime,
        to: ZonedDateTime
    ): Iterator[CrawlerExecution] =
      if onTheSameDay(from, to) then Iterator.single(createExecution(publisher, from, to))
      else
        val nextDay = nextDayOf(from)
        val head    = Iterator.single(createExecution(publisher, from, nextDay))
        val tail    = Iterator.unfold(Option(nextDay)) {
          case Some(current) if onTheSameDay(current, to) => Some((createExecution(publisher, current, to), None))
          case Some(current)                              =>
            val nextDay = nextDayOf(current)
            Some((createExecution(publisher, current, nextDay), Some(nextDay)))
          case None                                       => None
        }

        head ++ tail

    private def onTheSameDay(a: ZonedDateTime, b: ZonedDateTime): Boolean =
      a.toLocalDate().compareTo(b.toLocalDate()) == 0

    private def nextDayOf(a: ZonedDateTime): ZonedDateTime =
      ZonedDateTime.of(a.toLocalDate().plusDays(1L), LocalTime.of(0, 0, 0, 0), UTCZoneId)
