package graboid

import com.softwaremill.macwire.wire
import graboid.protocol.AddCrawler
import graboid.protocol.CrawlerDescriptor
import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandExecution
import graboid.protocol.RemoveCrawler
import graboid.protocol.RunAll
import graboid.protocol.RunCrawler
import graboid.protocol.UpdateCrawler
import graboid.repository.TimelineRepository
import zio.Task
import zio.ZLayer
import zio.stream.ZSink

import CommandExecutor.CommandExecutionBuilder

trait CommandExecutor:

  def apply(command: GraboidCommand): Task[GraboidCommandExecution]

object CommandExecutor:

  def apply(
      crawlerManager: CrawlerManager,
      crawlerRepository: CrawlerRepository,
      timelineRepository: TimelineRepository
  ): CommandExecutor = wire[CommandExecutorImpl]

  object CommandExecutionBuilder:

    inline given (using descriptor: GraboidCommand): CommandExecutionBuilder =
      new CommandExecutionBuilder(descriptor)

  class CommandExecutionBuilder(
      val descriptor: GraboidCommand,
      val startTime: Long = System.currentTimeMillis()
  )

private[graboid] class CommandExecutorImpl(
    crawlerManager: CrawlerManager,
    crawlerRepository: CrawlerRepository,
    timelineRepository: TimelineRepository
) extends CommandExecutor:

  private def crawlerManagerLayer     = ZLayer.succeed(crawlerManager)
  private def crawlerRepositoryLayer  = ZLayer.succeed(crawlerRepository)
  private def timelineRepositoryLayer = ZLayer.succeed(timelineRepository)

  override def apply(command: GraboidCommand): Task[GraboidCommandExecution] =
    given GraboidCommand = command
    command match
      case AddCrawler(descriptor) => addCrawler(descriptor)

      case RemoveCrawler(name) => removeCrawler(name)

      case UpdateCrawler(name, descriptor, shouldRunNow) =>
        updateCrawler(name, descriptor, shouldRunNow)

      case RunCrawler(name) => runCrawler(name)

      case RunAll => runAll()

  private def build()(using builder: CommandExecutionBuilder): GraboidCommandExecution =
    GraboidCommandExecution(System.currentTimeMillis() - builder.startTime, builder.descriptor)

  private def addCrawler(
      descriptor: CrawlerDescriptor
  )(using CommandExecutionBuilder): Task[GraboidCommandExecution] =
    for _ <- crawlerRepository.add(descriptor)
    yield build()

  private def removeCrawler(name: String)(using CommandExecutionBuilder): Task[GraboidCommandExecution] =
    for _ <- crawlerRepository.remove(name)
    yield build()

  private def updateCrawler(name: String, descriptor: CrawlerDescriptor, shouldRunNow: Boolean)(
      using CommandExecutionBuilder
  ): Task[GraboidCommandExecution] =
    for _ <- crawlerRepository.update(descriptor)
    yield build()

  private def runCrawler(name: String)(using CommandExecutionBuilder): Task[GraboidCommandExecution] =
    val execution = for _ <- crawlerManager.run(name) yield build()
    execution.provideLayer(crawlerRepositoryLayer ++ timelineRepositoryLayer)

  private def runAll()(using CommandExecutionBuilder): Task[GraboidCommandExecution] =
    val execution =
      for _ <- crawlerManager.runAll().run(ZSink.collectAll)
      yield build()

    execution.provideLayer(crawlerRepositoryLayer ++ timelineRepositoryLayer)
