package graboid

import com.softwaremill.macwire.*
import graboid.CrawlerManager.CrawlerReport
import graboid.config.CrawlerManagerConfig
import graboid.fdsn.FDSNCrawler
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
    wire[CrawlerModuleImpl]
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
      .runAll()
      .provideLayer(databaseModule.timelineRepositoryLayer ++ databaseModule.crawlerRepositoryLayer)
