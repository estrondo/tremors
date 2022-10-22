package tremors.graboid

import tremors.graboid.fdsn.FDSNCrawler
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.ZLayer.apply
import tremors.graboid.config.CrawlerManagerConfig

import scala.util.Try
import zio.stream.ZStream
import tremors.graboid.CrawlerManager.CrawlerReport

trait CrawlerModule:

  def runManager(): ZStream[Any, Throwable, CrawlerReport]

object CrawlerModule:

  def apply(
      config: CrawlerManagerConfig,
      httpModule: HttpModule,
      kafkaModule: KafkaModule,
      databaseModule: DatabaseModule
  ): Task[CrawlerModule] = ZIO.attempt {
    CrawlerModuleImpl(config, httpModule, kafkaModule, databaseModule)
  }

private[graboid] class CrawlerModuleImpl(
    config: CrawlerManagerConfig,
    httpModule: HttpModule,
    kafkaModule: KafkaModule,
    databaseModule: DatabaseModule
) extends CrawlerModule:

  val supervisorCreator: CrawlerManager.SupervisorCreator = (descriptor, crawler) =>
    Try(CrawlerSupervisor(kafkaModule.producerLayer)(descriptor, crawler))

  val fdsnCrawlerCreator: CrawlerManager.FDSNCrawlerCreator = (descriptor) =>
    Try(FDSNCrawler(httpModule.serviceLayer)(descriptor))

  val crawlerManager = CrawlerManager(
    config.materialized,
    supervisorCreator,
    fdsnCrawlerCreator
  )

  override def runManager(): ZStream[Any, Throwable, CrawlerReport] =
    crawlerManager
      .start()
      .provideLayer(databaseModule.timelineRepositoryLayer ++ databaseModule.crawlerRepositoryLayer)
