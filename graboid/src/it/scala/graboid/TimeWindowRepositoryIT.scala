package graboid

import core.KeyGenerator
import farango.Database
import graboid.fixture.TimeWindowFixture
import graboid.layer.ArangoDBLayer
import graboid.layer.FarangoLayer
import zio.Scope
import zio.UIO
import zio.ZIO
import zio.test.Spec
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.TestResult
import zio.test.assertTrue
import farango.zio.ZEffect

import java.time.ZonedDateTime
import farango.DocumentCollection
import graboid.TimeWindowRepository.given

object TimeWindowRepositoryIT extends IT:
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("TimeWindowRepository with Arango's container")(
      test("it should insert into database a new TimeWindow.") {
        val shouldInsert     = TimeWindowFixture.createRandom()
        val expectedDocument = timeWindowToDocument(shouldInsert)

        for
          collection <- FarangoLayer.documentCollection(s"timewindow_${KeyGenerator.next4()}")
          repository  = TimeWindowRepository(collection)
          _          <- repository.add(shouldInsert)
          inserted   <-
            collection.get[TimeWindowRepository.Document, ZEffect](shouldInsert.key).some
        yield assertTrue(
          inserted == expectedDocument
        )
      },
      test("it should find a TimeWindow.") {
        val expected = TimeWindowFixture
          .createRandom()
          .copy(successes = 0L, failures = 0L)

        val beginning = expected.beginning.plusDays(1)
        val ending    = expected.ending.minusDays(1)

        shouldFindTimeWindow(expected, beginning, ending, Some(expected))
      },
      test("it should not find a TimeWindow(expected TimeWindow is later).") {
        val expected = TimeWindowFixture
          .createRandom()
          .copy(successes = 0L, failures = 0L)

        val beginning = expected.ending.plusDays(1)
        val ending    = expected.ending.plusDays(2)

        shouldFindTimeWindow(expected, beginning, ending, None)
      },
      test("it should not find a TimeWindow(expected TimeWindow is earlier).") {
        val expected = TimeWindowFixture
          .createRandom()
          .copy(successes = 0L, failures = 0L)

        val beginning = expected.beginning.minusDays(2)
        val ending    = expected.beginning.minusDays(1)

        shouldFindTimeWindow(expected, beginning, ending, None)
      },
      test("it should not find a TimeWindow(expected TimeWindow is larger).") {
        val expected = TimeWindowFixture
          .createRandom()
          .copy(successes = 0L, failures = 0L)

        val beginning = expected.beginning.minusDays(2)
        val ending    = expected.ending.plusDays(2)

        shouldFindTimeWindow(expected, beginning, ending, None)
      },
      test("it should not find a TimeWindow(expected TimeWindow has the same limits).") {
        val expected = TimeWindowFixture
          .createRandom()
          .copy(successes = 0L, failures = 0L)

        val beginning = expected.beginning
        val ending    = expected.ending

        shouldFindTimeWindow(expected, beginning, ending, None)
      }
    ).provideLayer((ArangoDBLayer.layer >>> FarangoLayer.database)) @@ TestAspect.sequential

  private def shouldFindTimeWindow(
      shouldInsert: TimeWindow,
      beginning: ZonedDateTime,
      ending: ZonedDateTime,
      expected: Option[TimeWindow]
  ): ZIO[Database, Throwable, TestResult] =
    for
      collection  <- FarangoLayer.documentCollection(s"timeline_${KeyGenerator.next4()}")
      repository   = TimeWindowRepository(collection)
      _           <- repository.add(shouldInsert)
      publisherKey = shouldInsert.publisherKey
      result      <- repository.search(publisherKey, beginning, ending).orDie
    yield assertTrue(
      result == expected
    )
