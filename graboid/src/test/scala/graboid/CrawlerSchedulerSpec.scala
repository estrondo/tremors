package graboid

import graboid.fixture.CrawlerExecutionFixture
import graboid.fixture.PublisherFixture
import java.time.LocalTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import scala.annotation.tailrec
import testkit.core.createZonedDateTime
import zio.RIO
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue

object CrawlerSchedulerSpec extends Spec:

  def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A CrawlerScheduler")(
      test("It should return just one execution on the same day (with no last execution).") {
        val now       = createZonedDateTime().withHour(17)
        val reference = createZonedDateTime().withHour(21)

        for result <- withNoLastExecution(now, reference)
        yield
          val (publisher, iterator) = result
          val seq                   = iterator.toSeq

          assertTrue(
            seq.size == 1,
            seq.head == CrawlerExecution(
              key = seq.head.key,
              publisherKey = publisher.key,
              beginning = now,
              ending = reference
            )
          )
      },
      test("It should return just two execution for diffence of 1 day (with no last execution).") {
        val now       = createZonedDateTime().withHour(17)
        val reference = createZonedDateTime().plusDays(1).withHour(17)
        val middle    = ZonedDateTime.of(reference.toLocalDate(), LocalTime.of(0, 0, 0, 0), reference.getZone())

        for result <- withNoLastExecution(now, reference)
        yield
          val (publisher, iterator) = result
          val seq                   = iterator.toSeq

          assertTrue(
            seq.size == 2,
            seq.head == CrawlerExecution(
              key = seq.head.key,
              publisherKey = publisher.key,
              beginning = now,
              ending = middle
            ),
            seq.last == CrawlerExecution(
              key = seq.last.key,
              publisherKey = publisher.key,
              beginning = middle,
              ending = reference
            )
          )
      },
      test("It should return just 366 execution for difference of 365 days (with no last execution).") {
        val now            = createZonedDateTime().withHour(9)
        val reference      = createZonedDateTime().plusDays(365).withHour(17)
        val expectedMiddle = computeMiddle(now, reference)

        for result <- withNoLastExecution(now, reference)
        yield
          val (publisher, iterator) = result
          var seq                   = iterator.toSeq
          var head                  = seq.head
          val last                  = seq.last
          val middleSeq             = seq.drop(1).dropRight(1).map(x => (x.beginning, x.ending))
          val size                  = seq.size

          assertTrue(
            size == 366,
            head == CrawlerExecution(
              key = seq.head.key,
              publisherKey = publisher.key,
              beginning = now,
              ending = nextDayOf(now)
            ),
            last == CrawlerExecution(
              key = seq.last.key,
              publisherKey = publisher.key,
              beginning = midnightOfDay(reference),
              ending = reference
            ),
            middleSeq == expectedMiddle
          )
      },
      test("It should return just one execution on the same day (with last execution).") {
        val now       = createZonedDateTime().withHour(17)
        val reference = createZonedDateTime().withHour(21)

        for result <- withLastExecution(now, reference)
        yield
          val (publisher, iterator) = result
          val seq                   = iterator.toSeq

          assertTrue(
            seq.size == 1,
            seq.head == CrawlerExecution(
              key = seq.head.key,
              publisherKey = publisher.key,
              beginning = now,
              ending = reference
            )
          )
      },
      test("It should return just two execution for diffence of 1 day (with last execution).") {
        val now       = createZonedDateTime().withHour(17)
        val reference = createZonedDateTime().plusDays(1).withHour(17)
        val middle    = ZonedDateTime.of(reference.toLocalDate(), LocalTime.of(0, 0, 0, 0), reference.getZone())

        for result <- withLastExecution(now, reference)
        yield
          val (publisher, iterator) = result
          val seq                   = iterator.toSeq

          assertTrue(
            seq.size == 2,
            seq.head == CrawlerExecution(
              key = seq.head.key,
              publisherKey = publisher.key,
              beginning = now,
              ending = middle
            ),
            seq.last == CrawlerExecution(
              key = seq.last.key,
              publisherKey = publisher.key,
              beginning = middle,
              ending = reference
            )
          )
      },
      test("It should return just 366 execution for difference of 365 days (with last execution).") {
        val now            = createZonedDateTime().withHour(9)
        val reference      = createZonedDateTime().plusDays(365).withHour(17)
        val expectedMiddle = computeMiddle(now, reference)

        for result <- withLastExecution(now, reference)
        yield
          val (publisher, iterator) = result
          var seq                   = iterator.toSeq
          var head                  = seq.head
          val last                  = seq.last
          val middleSeq             = seq.drop(1).dropRight(1).map(x => (x.beginning, x.ending))
          val size                  = seq.size

          assertTrue(
            size == 366,
            head == CrawlerExecution(
              key = seq.head.key,
              publisherKey = publisher.key,
              beginning = now,
              ending = nextDayOf(now)
            ),
            last == CrawlerExecution(
              key = seq.last.key,
              publisherKey = publisher.key,
              beginning = midnightOfDay(reference),
              ending = reference
            ),
            middleSeq == expectedMiddle
          )
      }
    ).provideSomeLayer(CrawlerSchedulerLayer)

  private val CrawlerSchedulerLayer = ZLayer.succeed(CrawlerScheduler())

  def nextDayOf(a: ZonedDateTime) =
    ZonedDateTime.of(a.toLocalDate().plusDays(1), LocalTime.of(0, 0, 0, 0), a.getZone())

  def midnightOfDay(a: ZonedDateTime) =
    ZonedDateTime.of(a.toLocalDate(), LocalTime.of(0, 0, 0, 0), a.getZone())

  private def withNoLastExecution(
      now: ZonedDateTime,
      reference: ZonedDateTime
  ): RIO[CrawlerScheduler, (Publisher, Iterator[CrawlerExecution])] =
    val publisher = PublisherFixture
      .createRandom()
      .copy(beginning = now, ending = None)
    for
      scheduler <- ZIO.service[CrawlerScheduler]
      iterator  <- scheduler.computeSchedule(publisher, reference)
    yield publisher -> iterator

  private def withLastExecution(
      now: ZonedDateTime,
      reference: ZonedDateTime
  ): RIO[CrawlerScheduler, (Publisher, Iterator[CrawlerExecution])] =
    val publisher = PublisherFixture
      .createRandom()
      .copy(beginning = now, ending = None)

    val execution = CrawlerExecutionFixture
      .createRandom()
      .copy(publisherKey = publisher.key, ending = reference)
    for
      scheduler <- ZIO.service[CrawlerScheduler]
      iterator  <- scheduler.computeSchedule(publisher, reference)
    yield publisher -> iterator

  private def computeMiddle(from: ZonedDateTime, to: ZonedDateTime): Seq[(ZonedDateTime, ZonedDateTime)] =
    val stop = ZonedDateTime.of(to.toLocalDate(), LocalTime.of(0, 0, 0, 0), to.getZone())

    @tailrec
    def compute(
        actual: ZonedDateTime,
        result: Seq[(ZonedDateTime, ZonedDateTime)]
    ): Seq[(ZonedDateTime, ZonedDateTime)] =
      if actual.toLocalDate().compareTo(stop.toLocalDate()) == 0 then result
      else
        val nextDay = nextDayOf(actual)
        compute(nextDay, result :+ (actual -> nextDay))

    compute(nextDayOf(from), Vector.empty)
