package graboid

import com.softwaremill.macwire.wire
import graboid.protocol.AddCrawler
import graboid.protocol.CommandDescriptor
import graboid.protocol.CommandExecution
import graboid.protocol.CrawlerDescriptor
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

  def apply(command: CommandDescriptor): Task[CommandExecution]

object CommandExecutor:

  def apply(
      crawlerManager: CrawlerManager,
      crawlerRepository: CrawlerRepository,
      timelineRepository: TimelineRepository
  ): CommandExecutor = wire[CommandExecutorImpl]

  object CommandExecutionBuilder:

    inline given (using descriptor: CommandDescriptor): CommandExecutionBuilder =
      new CommandExecutionBuilder(descriptor)

  class CommandExecutionBuilder(
      val descriptor: CommandDescriptor,
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

  override def apply(command: CommandDescriptor): Task[CommandExecution] =
    given CommandDescriptor = command
    command match
      case AddCrawler(descriptor) => addCrawler(descriptor)

      case RemoveCrawler(name) => removeCrawler(name)

      case UpdateCrawler(name, descriptor, shouldRunNow) =>
        updateCrawler(name, descriptor, shouldRunNow)

      case RunCrawler(name) => runCrawler(name)

      case RunAll => runAll()

  private def build()(using builder: CommandExecutionBuilder): CommandExecution =
    CommandExecution(System.currentTimeMillis() - builder.startTime, builder.descriptor)

  private def addCrawler(
      descriptor: CrawlerDescriptor
  )(using CommandExecutionBuilder): Task[CommandExecution] =
    for _ <- crawlerRepository.add(descriptor)
    yield build()

  private def removeCrawler(name: String)(using CommandExecutionBuilder): Task[CommandExecution] =
    for _ <- crawlerRepository.remove(name)
    yield build()

  private def updateCrawler(name: String, descriptor: CrawlerDescriptor, shouldRunNow: Boolean)(
      using CommandExecutionBuilder
  ): Task[CommandExecution] =
    for _ <- crawlerRepository.update(descriptor)
    yield build()

  private def runCrawler(name: String)(using CommandExecutionBuilder): Task[CommandExecution] =
    val execution = for _ <- crawlerManager.run(name) yield build()
    execution.provideLayer(crawlerRepositoryLayer ++ timelineRepositoryLayer)

  private def runAll()(using CommandExecutionBuilder): Task[CommandExecution] =
    val execution =
      for _ <- crawlerManager.runAll().run(ZSink.collectAll)
      yield build()

    execution.provideLayer(crawlerRepositoryLayer ++ timelineRepositoryLayer)
