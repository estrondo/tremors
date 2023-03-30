package graboid

import core.KeyGenerator
import farango.DocumentCollection
import farango.zio.given
import graboid.CrawlerExecutionRepository.given
import graboid.fixture.CrawlerExecutionFixture
import graboid.fixture.PublisherFixture
import java.time.ZonedDateTime
import scala.annotation.newMain
import scala.annotation.tailrec
import testkit.core.createZonedDateTime
import testkit.zio.testcontainers.ArangoDBLayer
import testkit.zio.testcontainers.FarangoLayer
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.assertTrue

object CrawlerExecutionRepositoryIT extends IT:

  import CrawlerExecutionRepository.Document

  def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("A CrawlerExecutionRepository")(
      suite("It with Arango's container")(
        test("It should add a execution into a collection.") {
          val publisherKey = KeyGenerator.next64()

          val expected = CrawlerExecutionFixture.createRandom()

          for
            repository <- ZIO.service[CrawlerExecutionRepository]
            _          <- repository.add(expected)
            collection <- ZIO.service[DocumentCollection]
            added      <- collection.get[Document](expected.key).some
          yield assertTrue(
            added == expected
          )
        },
        test("It should search the last execution in collection.") {
          val publisher = PublisherFixture.createRandom()

          val toInsert = generateToInsert(100, createZonedDateTime().withHour(13))
            .map(_.copy(publisherKey = publisher.key))

          val expected = toInsert.last

          for
            repository    <- ZIO.service[CrawlerExecutionRepository]
            _             <- ZIO.foreach(toInsert)(repository.add)
            lastExecution <- repository.searchLast(publisher).some
          yield assertTrue(
            lastExecution == expected
          )
        },
        test("It should remove executions by publisherKey.") {
          val publisher = PublisherFixture.createRandom()

          val toInsert = generateToInsert(100, createZonedDateTime().withHour(13))
            .map(_.copy(publisherKey = publisher.key))

          val publisherKeyToRemove = KeyGenerator.next64()
          val toRemove             = generateToInsert(100, createZonedDateTime().withHour(13))
            .map(_.copy(publisherKey = publisherKeyToRemove))

          val expected = toInsert.last

          for
            repository     <- ZIO.service[CrawlerExecutionRepository]
            collection     <- ZIO.service[DocumentCollection]
            _              <- ZIO.foreach(toInsert ++ toRemove)(repository.add)
            removeStream   <- repository.removeWithPublisherKey(publisherKeyToRemove)
            removedCount   <- removeStream.runCount
            documentsStream = collection.documents[Document]()
            total          <- documentsStream.runCount
          yield assertTrue(
            removedCount == 100L,
            total == 100L
          )
        },
        test("It should update an execution in collection.") {
          val publisherKey = KeyGenerator.next64()

          val toInsertStarted = createZonedDateTime()
          val toInsertStop    = toInsertStarted.plusMinutes(5)
          val toInsertStopped = toInsertStarted.plusMinutes(7)

          val toInsert = CrawlerExecutionFixture
            .createRandom()
            .copy(
              status = Some(CrawlerExecution.Status.Running),
              executionStarted = Some(toInsertStarted),
              expectedStop = Some(toInsertStop),
              executionStopped = Some(toInsertStopped),
              message = Some("Woohoo!")
            )

          val expectedStarted = createZonedDateTime().plusMinutes(29)
          val expectedStop    = expectedStarted.plusMinutes(13)
          val expectedStopped = expectedStarted.plusMinutes(19)

          val expected = toInsert
            .copy(
              status = Some(CrawlerExecution.Status.Completed),
              executionStarted = Some(expectedStarted),
              expectedStop = Some(expectedStop),
              executionStopped = Some(expectedStopped),
              message = Some("I'm inevitable")
            )

          for
            repository <- ZIO.service[CrawlerExecutionRepository]
            _          <- repository.add(toInsert)
            _          <- repository.update(expected)
            collection <- ZIO.service[DocumentCollection]
            updated    <- collection.get[Document](toInsert.key).some
          yield assertTrue(
            updated == expected
          )
        }
      ).provideSome(
        ArangoDBLayer.layer,
        FarangoLayer.database,
        FarangoLayer.documentCollectionLayer(s"crawler_execution_${KeyGenerator.next4()}"),
        CrawlerExecutionRepositoryLayer
      ) @@ TestAspect.sequential
    )

  private val CrawlerExecutionRepositoryLayer = ZLayer {
    ZIO.serviceWith[DocumentCollection](CrawlerExecutionRepository.apply)
  }

  @tailrec
  private def generateToInsert(
      num: Int,
      current: ZonedDateTime,
      result: Seq[CrawlerExecution] = Vector.empty
  ): Seq[CrawlerExecution] =
    if result.size < num then
      val next         = current.plusDays(1)
      val newExecution = CrawlerExecutionFixture
        .createRandom()
        .copy(beginning = current, ending = next)

      generateToInsert(num, next, result :+ newExecution)
    else result
