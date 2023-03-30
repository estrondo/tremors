package graboid

import com.softwaremill.macwire.wire
import graboid.config.CrawlerExecutorConfig
import zio.Fiber
import zio.Schedule
import zio.Task
import zio.UIO
import zio.ZIO

trait CrawlerExecutorModule:

  def crawlerExecutor: CrawlerExecutor

  def run(): Task[CrawlingReport]

  def start(): UIO[Fiber.Runtime[Throwable, Long]]

object CrawlerExecutorModule:

  def apply(
      config: CrawlerExecutorConfig,
      repositoryModule: RepositoryModule,
      coreModule: CoreModule,
      httpModule: HttpModule
  ): Task[CrawlerExecutorModule] =
    ZIO.attempt(wire[Impl])

  private class Impl(
      config: CrawlerExecutorConfig,
      repositoryModule: RepositoryModule,
      coreModule: CoreModule,
      httpModule: HttpModule
  ) extends CrawlerExecutorModule:

    private val scheduler = CrawlerScheduler()

    private val crawlerFactory = CrawlerFactory(
      httpLayer = httpModule.serviceLayer
    )

    val crawlerExecutor: CrawlerExecutor = CrawlerExecutor(
      repository = repositoryModule.crawlerExecutionRepository,
      scheduler = scheduler,
      publisherManager = coreModule.publisherManager,
      eventManager = coreModule.eventManager,
      crawlerFactory = crawlerFactory
    )

    override def run(): Task[CrawlingReport] =
      crawlerExecutor.run()

    override def start(): UIO[Fiber.Runtime[Throwable, Long]] =
      run()
        .repeat(Schedule.forever >>> Schedule.spaced(config.interval))
        .fork
