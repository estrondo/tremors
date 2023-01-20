package graboid

import core.KeyGenerator
import farango.FarangoDatabase
import farango.FarangoDocumentCollection
import graboid.fixture.EventRecordFixture
import graboid.layer.ArangoDBLayer
import graboid.layer.FarangoLayer
import graboid.query.TimeWindowLink
import zio.RIO
import zio.Scope
import zio.UIO
import zio.URLayer
import zio.ZIO
import zio.ZLayer
import zio.stream.ZSink
import zio.test.Annotations
import zio.test.TestAspect
import zio.test.TestEnvironment
import zio.test.TestResult
import zio.test.assertTrue
import zio.test.ignored
import ziorango.Ziorango
import ziorango.given

object EventRecordRepositoryIT extends IT:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("EventRecordRepository with Arango's container.")(
      test("it should insert into Arango an EventRecord.") {
        for
          repository          <- getRepository
          expectedEventRecord  = EventRecordFixture.createRandom()
          insertedEventRecord <- repository.add(expectedEventRecord).orDie
          collection          <- getCollection
          _                   <- collection
                                   .get[EventRecordRepository.Document, Ziorango.F](expectedEventRecord.key)
                                   .someOrFail("it was supposed to find something!")
        yield assertTrue(
          expectedEventRecord == insertedEventRecord
        )
      },
      test("it should find all linked-timewindow EventRecord related to a publisher.") {
        val publisherKey  = KeyGenerator.next64()
        val timeWindowKey = KeyGenerator.next64()

        val expected = EventRecordFixture.createRandomSeq(10)(
          _.copy(publisherKey = publisherKey, timeWindowKey = Some(timeWindowKey))
        )

        val unexpected = EventRecordFixture.createRandomSeq(10)(_.copy(publisherKey = publisherKey))

        shouldSearchByPublisher(
          expected ++ unexpected,
          expected,
          publisherKey,
          Some(TimeWindowLink.With(timeWindowKey))
        )
      },
      test("it should find all not-linked-timewindow EventRecord related to a publisher") {
        val publisherKey = KeyGenerator.next64()

        val expected = EventRecordFixture.createRandomSeq(3)(
          _.copy(publisherKey = publisherKey, timeWindowKey = None)
        )

        val unexpected = EventRecordFixture.createRandomSeq(3)(
          _.copy(publisherKey = publisherKey)
        )

        shouldSearchByPublisher(
          expected ++ unexpected,
          expected,
          publisherKey,
          Some(TimeWindowLink.Unliked)
        )
      },
      test("it should find all timewindow EventRecord related with a publisher") {
        val publisherKey = KeyGenerator.next64()

        val expected = EventRecordFixture.createRandomSeq(3)(
          _.copy(publisherKey = publisherKey)
        ) ++ EventRecordFixture.createRandomSeq(3)(
          _.copy(publisherKey = publisherKey, timeWindowKey = None)
        )

        val unexpected = EventRecordFixture
          .createRandomSeq(3)(identity)

        shouldSearchByPublisher(
          expected ++ unexpected,
          expected,
          publisherKey,
          None
        )
      }
    ).provideSomeLayer(
      ArangoDBLayer.layer >>> FarangoLayer.database >>> TestLayer
    ) @@ TestAspect.sequential

  private def shouldSearchByPublisher(
      toInsert: Seq[EventRecord],
      expected: Seq[EventRecord],
      publisherKey: String,
      link: Option[TimeWindowLink]
  ): RIO[TestLayer, TestResult] =
    for
      repository <- getRepository
      inserteds  <- ZIO.foreachPar(toInsert)(repository.add)
      stream     <- repository
                      .searchByPublisher(publisherKey, link)
      result     <- stream.run(ZSink.collectAll)
    yield assertTrue(
      inserteds.size == toInsert.size,
      result.size == expected.size,
      result.toSet == expected.toSet
    )

  type TestLayer = (EventRecordRepository, FarangoDocumentCollection)

  private val getCollection: RIO[TestLayer, FarangoDocumentCollection] =
    for tuple <- ZIO.service[TestLayer]
    yield tuple._2

  private val getRepository: RIO[TestLayer, EventRecordRepository] =
    for tuple <- ZIO.service[TestLayer]
    yield tuple._1

  private val TestLayer: ZLayer[FarangoDatabase, Throwable, TestLayer] = ZLayer {
    for collection <- FarangoLayer.documentCollection(s"event_record_${KeyGenerator.next4()}")
    yield (EventRecordRepository(collection), collection)
  }
