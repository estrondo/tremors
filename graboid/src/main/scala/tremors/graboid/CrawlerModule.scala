package tremors.graboid

import tremors.graboid.fdsn.FDSNCrawler
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.ZLayer.apply

import scala.util.Try

trait CrawlerModule:

  def runManager(): ZIO[Any, Throwable, String]

object CrawlerModule:

  def apply(config: CrawlerManager.Config): Task[CrawlerModule] =
    ???

private[graboid] class CrawlerModuleImpl(
    config: CrawlerManager.Config,
    httpModule: HttpModule,
    kafkaModule: KafkaModule,
    databaseModule: DatabaseModule
) extends CrawlerModule:

  val timelineManager: TimelineManager.Layer              = ZLayer.fromZIO(???)
  val supervisorCreator: CrawlerManager.SupervisorCreator = (descriptor, crawler) =>
    Try(CrawlerSupervisor(kafkaModule.producerLayer)(descriptor, crawler))

  val fdsnCrawlerCreator: CrawlerManager.FDSNCrawlerCreator = (descriptor) =>
    Try(FDSNCrawler(httpModule.serviceLayer)(descriptor))

  private def crawlerManager = CrawlerManager(
    config,
    supervisorCreator,
    fdsnCrawlerCreator
  )

  override def runManager(): ZIO[Any, Throwable, String] =
    ???
