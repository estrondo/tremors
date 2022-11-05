package graboid

import graboid.protocol.AddCrawler
import graboid.protocol.RemoveCrawler
import graboid.protocol.RunAll
import graboid.protocol.RunCrawler
import graboid.protocol.UpdateCrawler
import org.mockito.ArgumentMatchers.{eq => mEq}
import org.mockito.Mockito
import org.mockito.Mockito.verify
import graboid.command.CrawlerDescriptorFixture
import graboid.repository.TimelineRepository
import zio.ULayer
import zio.URIO
import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream
import zio.test.assertTrue

object CommandExecutorSpec extends Spec:

  private val crawlerRepositoryMockLayer: ULayer[CrawlerRepository] =
    ZLayer(ZIO.succeed(Mockito.mock(classOf[CrawlerRepository])))

  private val timelineRepositoryMockLayer: ULayer[TimelineRepository] =
    ZLayer(ZIO.succeed(Mockito.mock(classOf[TimelineRepository])))

  private val crawlerManagerMockLayer: ULayer[CrawlerManager] =
    ZLayer(ZIO.succeed(Mockito.mock(classOf[CrawlerManager])))

  private val allMockLayer =
    crawlerManagerMockLayer ++ crawlerRepositoryMockLayer ++ timelineRepositoryMockLayer

  private def createExecutor()
      : URIO[CrawlerManager & CrawlerRepository & TimelineRepository, CommandExecutor] =
    for
      crawlerManager     <- ZIO.service[CrawlerManager]
      crawlerRepository  <- ZIO.service[CrawlerRepository]
      timelineRepository <- ZIO.service[TimelineRepository]
    yield CommandExecutor(crawlerManager, crawlerRepository, timelineRepository)

  override def spec = suite("A CommandExecutor")(
    test("should accept a AddCrawler") {
      for
        repository       <- ZIO.service[CrawlerRepository]
        crawlerDescriptor = CrawlerDescriptorFixture.createRandom()
        addCrawler        = AddCrawler(crawlerDescriptor)
        _                 = Mockito
                              .when(repository.add(mEq(crawlerDescriptor)))
                              .thenReturn(ZIO.succeed(crawlerDescriptor))
        executor         <- createExecutor()
        execution        <- executor(addCrawler)
      yield assertTrue(
        execution.descriptor == addCrawler,
        verify(repository).add(mEq(crawlerDescriptor)) == null
      )
    }.provideLayer(allMockLayer),
    test("should accept a RemoveCrawler") {
      for
        repository       <- ZIO.service[CrawlerRepository]
        crawlerDescriptor = CrawlerDescriptorFixture.createRandom()
        removeCrawler     = RemoveCrawler(name = "a-test-crawler")
        _                 = Mockito
                              .when(repository.remove(removeCrawler.name))
                              .thenReturn(ZIO.succeed(Some(crawlerDescriptor)))
        executor         <- createExecutor()
        execution        <- executor(removeCrawler)
      yield assertTrue(
        execution.descriptor == removeCrawler,
        verify(repository).remove(removeCrawler.name) == null
      )
    }.provideLayer(allMockLayer),
    test("should accept a UpdateCrawler") {
      for
        repository       <- ZIO.service[CrawlerRepository]
        crawlerDescriptor = CrawlerDescriptorFixture.createRandom()
        updateCrawler     = UpdateCrawler("a-test-crawler", crawlerDescriptor, shouldRunNow = false)
        _                 = Mockito
                              .when(repository.update(mEq(crawlerDescriptor)))
                              .thenReturn(ZIO.succeed(Some(crawlerDescriptor)))
        executor         <- createExecutor()
        execution        <- executor(updateCrawler)
      yield assertTrue(
        execution.descriptor == updateCrawler,
        verify(repository).update(mEq(crawlerDescriptor)) == null
      )
    }.provideLayer(allMockLayer),
    test("should accept a RunCrawler") {
      for
        manager   <- ZIO.service[CrawlerManager]
        report     = CrawlerReportFixture.createRandom(CrawlerDescriptorFixture.createRandom())
        runCrawler = RunCrawler("a-test-crawler")
        _          = Mockito
                       .when(manager.run(mEq(runCrawler.name)))
                       .thenReturn(ZIO.succeed(Some(report)))
        executor  <- createExecutor()
        execution <- executor(runCrawler)
      yield assertTrue(
        execution.descriptor == runCrawler,
        verify(manager).run(mEq(runCrawler.name)) == null
      )
    }.provideLayer(allMockLayer),
    test("should accept a RunAll") {
      for
        manager   <- ZIO.service[CrawlerManager]
        report     = CrawlerReportFixture.createRandom(CrawlerDescriptorFixture.createRandom())
        _          = Mockito
                       .when(manager.runAll())
                       .thenReturn(ZStream(report))
        executor  <- createExecutor()
        execution <- executor(RunAll)
      yield assertTrue(
        execution.descriptor == RunAll,
        verify(manager).runAll() == null
      )
    }.provideLayer(allMockLayer)
  )
