package graboid.module

import com.softwaremill.macwire.Module
import graboid.config.CrawlingConfig
import graboid.config.EventCrawlingConfig
import graboid.crawling.CrawlingExecutor
import graboid.crawling.CrawlingScheduler
import graboid.crawling.CrawlingScheduler.EventConfig
import graboid.crawling.EventCrawler
import graboid.time.ZonedDateTimeService
import io.github.arainko.ducktape.into
import tremors.generator.KeyGenerator
import zio.Schedule
import zio.Task
import zio.ZIO
import zio.http.Client

@Module
trait CrawlingModule:

  val crawlingExecutor: CrawlingExecutor

  val crawlingScheduler: CrawlingScheduler

  def start(): Task[Unit]

object CrawlingModule:

  def apply(
      config: CrawlingConfig,
      httpModule: HttpModule,
      kafkaModule: KafkaModule,
      managerModule: ManagerModule,
      repositoryModule: RepositoryModule
  ): Task[CrawlingModule] =

    val crawlingExecutor = CrawlingExecutor(
      repositoryModule.crawlingExecutionRepository,
      managerModule.dataCentreManager,
      EventCrawler.Factory,
      KeyGenerator,
      ZonedDateTimeService.Impl
    )

    for scheduler <- CrawlingScheduler(repositoryModule.dataStore, crawlingExecutor)
    yield new Impl(config, httpModule, kafkaModule, crawlingExecutor, scheduler)

  private def convert(event: EventCrawlingConfig): Task[EventConfig] =
    ZIO.attempt {
      event.into[EventConfig].transform()
    }

  private class Impl(
      config: CrawlingConfig,
      httpModule: HttpModule,
      kafkaModule: KafkaModule,
      val crawlingExecutor: CrawlingExecutor,
      val crawlingScheduler: CrawlingScheduler
  ) extends CrawlingModule:

    override def start(): Task[Unit] =
      for
        eventConfig <- convert(config.event)
        eventResult  = crawlingScheduler
                         .start(eventConfig)
                         .repeat(Schedule.spaced(eventConfig.interval))
                         .runDrain
                         .provideSome(
                           kafkaModule.producerLayer,
                           ZonedDateTimeService.live,
                           httpModule.client
                         )
        _           <- eventResult
      yield ()
