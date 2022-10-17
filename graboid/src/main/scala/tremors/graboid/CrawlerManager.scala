package tremors.graboid

import tremors.graboid.fdsn.FDSNCrawler
import tremors.graboid.repository.TimelineRepository
import zio.Task
import zio.TaskLayer
import zio.UIO
import zio.ULayer
import zio.URIO
import zio.ZEnvironment
import zio.ZIO
import zio.ZLayer
import zio.kafka.producer.Producer
import zio.stream.ZStream

import java.net.URI
import java.net.URL
import scala.util.Try

import CrawlerManager.*

trait CrawlerManager:

  def start(): ZStream[CrawlerRepository & TimelineRepository, Throwable, CrawlerReport]

object CrawlerManager:

  type SupervisorCreator  = (CrawlerDescriptor, Crawler) => Try[CrawlerSupervisor]
  type FDSNCrawlerCreator = (CrawlerDescriptor) => Try[Crawler]

  case class Config(concurrency: Int = math.max(Runtime.getRuntime().availableProcessors() / 2, 1)):
    require(concurrency > 0, s"Invalid concurrency $concurrency!")

  case class CrawlerReport(
      name: String,
      `type`: String,
      source: String,
      success: Long,
      fail: Long
  )

  def apply(
      config: Config,
      supervisorCreator: SupervisorCreator,
      fdsnCrawlerCreator: FDSNCrawlerCreator
  ): CrawlerManager =
    CrawlerManagerImpl(config, supervisorCreator, fdsnCrawlerCreator)

private[graboid] class CrawlerManagerImpl(
    config: Config,
    supervisorCreator: SupervisorCreator,
    fdsnCrawlerCreator: FDSNCrawlerCreator
) extends CrawlerManager:

  override def start(): ZStream[CrawlerRepository & TimelineRepository, Throwable, CrawlerReport] =
    for
      crawlerRepository <- ZStream.service[CrawlerRepository]
      report            <- crawlerRepository.getAllDescriptors().mapZIOPar(config.concurrency)(handle)
    yield report

  private def handle(
      descriptor: CrawlerDescriptor
  ): ZIO[TimelineRepository, Throwable, CrawlerReport] =
    for
      _          <- ZIO.logInfo(s"Creating CrawlerSupervisor: ${descriptor.name}")
      supervisor <- createSupervisor(descriptor)
      _          <- ZIO.logInfo(s"Starting CrawlerSupervisor: ${descriptor.name}")
      status     <- supervisor.run().provideLayer(provideTimelineManager(descriptor))
      _          <- ZIO.logInfo(s"CrawlerSupervisor ${descriptor.name} has found ${status.success} events.")
    yield CrawlerReport(
      name = descriptor.name,
      `type` = descriptor.`type`,
      source = descriptor.source,
      success = status.success,
      fail = status.fail
    )

  private def createSupervisor(descriptor: CrawlerDescriptor): Task[CrawlerSupervisor] =
    for
      crawler    <- createCrawler(descriptor)
      supervisor <- ZIO.fromTry(supervisorCreator(descriptor, crawler))
    yield supervisor

  private def createCrawler(descriptor: CrawlerDescriptor): Task[Crawler] =
    descriptor.`type` match
      case FDSNCrawler.TypeName => ZIO.fromTry(fdsnCrawlerCreator(descriptor))
      case typeName             => ZIO.fail(GraboidException.Invalid(s"Invalid CrawlerType: $typeName"))

  private def provideTimelineManager(
      descriptor: CrawlerDescriptor
  ): ZLayer[TimelineRepository, Throwable, TimelineManager] =
    ZLayer {
      for repository <- ZIO.service[TimelineRepository]
      yield TimelineManager(
        windowDuration = descriptor.windowDuration,
        starting = descriptor.starting,
        repository = repository
      )
    }
