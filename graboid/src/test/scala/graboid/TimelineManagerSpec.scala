package graboid

import graboid.fixture.EventRecordFixture
import graboid.fixture.TimeWindowFixture
import graboid.query.TimeWindowLink
import org.mockito.ArgumentMatchers.any
import testkit.core.SweetMockito
import zio.Scope
import zio.ULayer
import zio.URIO
import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream
import zio.test.TestEnvironment
import zio.test.assertTrue

import java.net.URI

object TimelineManagerSpec extends Spec:

  override def spec: zio.test.Spec[TestEnvironment & Scope, Any] =
    suite("TimelineManager with mocking")(
      test("it should add an EventRecord into EventRecordRepository.") {
        for
          eventRecordRepository <- getEventRecordRepository
          manager               <- getTimelineManager
          expectedEventRecord    = EventRecordFixture.createRandom()
          _                      = SweetMockito
                                     .returnF(eventRecordRepository.add(expectedEventRecord))(expectedEventRecord)
          result                <- manager.addRecord(expectedEventRecord)
        yield assertTrue(
          result == expectedEventRecord
        )
      },
      test("it should create a new TimeWindow into TimeWindowRepository.") {
        val expected = TimeWindowFixture
          .createRandom()
        for
          timeWindowRepository  <- getTimeWindowRepository
          eventRecordRepository <- getEventRecordRepository
          manager               <- getTimelineManager
          _                      = SweetMockito
                                     .returnF(
                                       timeWindowRepository.search(
                                         expected.publisherKey,
                                         expected.beginning,
                                         expected.ending
                                       )
                                     )(None)
          _                      = SweetMockito
                                     .returnF(timeWindowRepository.add(any()))(expected)
          _                      = SweetMockito
                                     .returnF(
                                       eventRecordRepository.searchByPublisher(
                                         expected.publisherKey,
                                         Some(TimeWindowLink.Unliked)
                                       )
                                     )(ZStream.empty)
          result                <- manager
                                     .createWindow(expected.publisherKey, expected.beginning, expected.ending)
        yield assertTrue(
          result == expected
        )
      },
      test("it should link all unliked EventRecord to a new TimeWindow.") {
        val unlikedSeq = EventRecordFixture
          .createRandomSeq(10)(_.copy(timeWindowKey = None))
        val timeWindow = TimeWindowFixture
          .createRandom()
          .copy(
            successes = 0L,
            failures = 0L
          )

        val expected = timeWindow.copy(successes = unlikedSeq.size)

        for
          timeWindowRepository  <- getTimeWindowRepository
          eventRecordRepository <- getEventRecordRepository
          manager               <- getTimelineManager
          _                      = SweetMockito
                                     .returnF(
                                       timeWindowRepository.search(
                                         publisherKey = timeWindow.publisherKey,
                                         beginning = timeWindow.beginning,
                                         ending = timeWindow.ending
                                       )
                                     )(None)
          _                      = SweetMockito
                                     .returnF(timeWindowRepository.add(any()))(timeWindow)
          _                      = SweetMockito
                                     .returnF(
                                       eventRecordRepository.searchByPublisher(
                                         timeWindow.publisherKey,
                                         Some(TimeWindowLink.Unliked)
                                       )
                                     )(ZStream.fromIterable(unlikedSeq))
          _                      = SweetMockito
                                     .answerF(eventRecordRepository.update(any()))(invocation => invocation.getArgument[EventRecord](0))
          _                      = SweetMockito
                                     .returnF(timeWindowRepository.update(expected))(expected)
          result                <-
            manager
              .createWindow(timeWindow.publisherKey, timeWindow.beginning, timeWindow.ending)
        yield assertTrue(
          result == expected
        )
      }
    ).provideSomeLayer(TestLayer)

  private type TestLayer = (TimelineManager, TimeWindowRepository, EventRecordRepository)

  private val getTimelineManager: URIO[TestLayer, TimelineManager] =
    for tuple <- ZIO.service[TestLayer]
    yield tuple._1

  private val getTimeWindowRepository: URIO[TestLayer, TimeWindowRepository] =
    for tuple <- ZIO.service[TestLayer]
    yield tuple._2

  private val getEventRecordRepository: URIO[TestLayer, EventRecordRepository] =
    for tuple <- ZIO.service[TestLayer]
    yield tuple._3

  private val TestLayer: ULayer[TestLayer] = ZLayer.fromFunction { (_: Any) =>
    val timeWindowRepository  = SweetMockito[TimeWindowRepository]
    val eventRecordRepository = SweetMockito[EventRecordRepository]
    val timelineManager       = TimelineManager(timeWindowRepository, eventRecordRepository)
    (timelineManager, timeWindowRepository, eventRecordRepository)
  }
