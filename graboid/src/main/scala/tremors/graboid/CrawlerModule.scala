package tremors.graboid

import com.softwaremill.macwire.*
import tremors.graboid.CrawlerManager.CrawlerReport
import tremors.graboid.config.CrawlerManagerConfig
import tremors.graboid.fdsn.FDSNCrawler
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.ZLayer.apply
import zio.stream.ZStream

import scala.util.Try

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

  val crawlerManager = wireWith(CrawlerManager.apply)

  private def crawlerManagerConfig = config.materialized

  override def runManager(): ZStream[Any, Throwable, CrawlerReport] =
    crawlerManager
      .start()
      .provideLayer(databaseModule.timelineRepositoryLayer ++ databaseModule.crawlerRepositoryLayer)
