package tremors.graboid

import tremors.graboid.fdsn.FDSNCrawler
import zio.Task
import zio.ULayer
import zio.URIO
import zio.ZIO
import zio.ZLayer
import zio.ZLayer.apply
import zio.kafka.producer.Producer
import zio.stream.ZStream

import java.net.URI
import java.net.URL
import scala.util.Try

import CrawlerManager.*

trait CrawlerManager:

  def start(): ZStream[CrawlerRepository, Throwable, CrawlerReport]

object CrawlerManager:

  type SupervisorCreator  = (CrawlerSupervisor.Config, Crawler) => Try[CrawlerSupervisor]
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
      timelineManager: TimelineManager.Layer,
      supervisorCreator: SupervisorCreator,
      fdsnCrawlerCreator: FDSNCrawlerCreator
  ): CrawlerManager =
    CrawlerManagerImpl(config, timelineManager, supervisorCreator, fdsnCrawlerCreator)

private[graboid] class CrawlerManagerImpl(
    config: Config,
    timelineManager: TimelineManager.Layer,
    supervisorCreator: SupervisorCreator,
    fdsnCrawlerCreator: FDSNCrawlerCreator
) extends CrawlerManager:

  override def start(): ZStream[CrawlerRepository, Throwable, CrawlerReport] =
    for
      repository <- ZStream.service[CrawlerRepository]
      report     <- repository.getAllDescriptors().mapZIOPar(config.concurrency)(handle)
    yield report

  private def handle(descriptor: CrawlerDescriptor): ZIO[Any, Throwable, CrawlerReport] =
    for
      _          <- ZIO.logInfo(s"Creating CrawlerSupervisor: ${descriptor.name}")
      supervisor <- createSupervisor(descriptor)
      _          <- ZIO.logInfo(s"Starting CrawlerSupervisor: ${descriptor.name}")
      status     <- supervisor.start().provideLayer(timelineManager)
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
      config      = CrawlerSupervisor.Config(descriptor.name)
      supervisor <- ZIO.fromTry(supervisorCreator(config, crawler))
    yield supervisor

  private def createCrawler(descriptor: CrawlerDescriptor): Task[Crawler] =
    descriptor.`type` match
      case FDSNCrawler.TypeName => ZIO.fromTry(fdsnCrawlerCreator(descriptor))
      case typeName             => ZIO.fail(GraboidException.Invalid(s"Invalid CrawlerType: $typeName"))
