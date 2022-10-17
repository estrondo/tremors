package tremors.graboid

import org.mockito.Mockito.*
import tremors.graboid.fdsn.FDSNCrawler
import tremors.graboid.repository.TimelineRepository
import zio.UIO
import zio.ZIO
import zio.ZLayer
import zio.stream.ZSink
import zio.stream.ZStream
import zio.test.assertTrue

import scala.collection.immutable.HashMap
import scala.util.Success
import scala.util.Try
import java.time.Duration
import java.time.ZonedDateTime

object CrawlerManagerSpec extends Spec:

  private val timelineManagerMockLayer = ZLayer.succeed {
    mock(classOf[TimelineManager])
  }

  private val timelineRepositoryMockLayer = ZLayer.succeed {
    mock(classOf[TimelineRepository])
  }

  private val crawlerRepositoryMockLayer = ZLayer.succeed {
    mock(classOf[CrawlerRepository])
  }

  private val mockLayer =
    timelineManagerMockLayer ++ timelineRepositoryMockLayer ++ crawlerRepositoryMockLayer

  private def createManager(
      timelineManager: TimelineManager.Layer,
      supervisorCreator: CrawlerManager.SupervisorCreator,
      fdsnCrawlerCreator: CrawlerManager.FDSNCrawlerCreator
  ): UIO[CrawlerManager] = ZIO.succeed {
    CrawlerManager(
      config = CrawlerManager.Config(),
      supervisorCreator = supervisorCreator,
      fdsnCrawlerCreator = fdsnCrawlerCreator
    )
  }

  override def spec = suite("A CrawlerManager")(
    test("when find some descriptor, it should create supervisors") {

      val earthDescriptor = CrawlerDescriptor(
        "#earth",
        FDSNCrawler.TypeName,
        "earth",
        Duration.ofDays(13),
        ZonedDateTime.now()
      )

      val marsDescriptor = CrawlerDescriptor(
        "#mars",
        FDSNCrawler.TypeName,
        "mars",
        Duration.ofDays(29),
        ZonedDateTime.now()
      )

      val descriptors = Seq(earthDescriptor, marsDescriptor)

      val earthCrawler = mock(classOf[Crawler])
      val marsCrawler  = mock(classOf[Crawler])

      val earthSupervisor = mock(classOf[CrawlerSupervisor])
      val marsSupervisor  = mock(classOf[CrawlerSupervisor])

      val crawlerMap    = HashMap(earthDescriptor -> earthCrawler, marsDescriptor -> marsCrawler)
      val supervisorMap = HashMap(earthCrawler -> earthSupervisor, marsCrawler -> marsSupervisor)

      val earthStatus = CrawlerSupervisor.Status(1000, 100, 10)
      val marsStatus  = CrawlerSupervisor.Status(500, 50, 5)

      for
        timelineManager   <- ZIO.service[TimelineManager]
        crawlerRepository <- ZIO.service[CrawlerRepository]
        _                  = when(crawlerRepository.getAllDescriptors())
                               .thenReturn(ZStream.fromIterable(descriptors))
        _                  = when(earthSupervisor.run()).thenReturn(ZIO.succeed(earthStatus))
        _                  = when(marsSupervisor.run()).thenReturn(ZIO.succeed(marsStatus))
        manager           <- createManager(
                               ZLayer.succeed(timelineManager),
                               supervisorCreator = (_, crawler) => Try(supervisorMap(crawler)),
                               fdsnCrawlerCreator = descriptor => Try(crawlerMap(descriptor))
                             )
        result            <- manager.start().run(ZSink.collectAll)
      yield assertTrue(
        result.map(_.success).sum == 1500L,
        result.map(_.fail).sum == 150L
      )
    }.provideLayer(mockLayer)
  )