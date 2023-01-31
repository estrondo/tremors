package graboid

import core.KeyGenerator
import farango.DocumentCollection
import farango.zio.given
import graboid.fixture.CrawlerExecutionFixture
import graboid.layer.ArangoDBLayer
import graboid.layer.FarangoLayer
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.TestEnvironment
import zio.test.assertTrue
import testkit.core.createZonedDateTime
import java.time.ZonedDateTime
import graboid.fixture.PublisherFixture
import scala.annotation.tailrec
import scala.annotation.newMain
import zio.test.TestAspect

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
            collection    <- ZIO.service[DocumentCollection]
            lastExecution <- repository.searchLast(publisher).some
          yield assertTrue(
            lastExecution == expected
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
