package tremors.graboid

import com.softwaremill.macwire.wire
import tremors.graboid.command.*
import tremors.graboid.repository.TimelineRepository
import zio.Task
import zio.ZEnvironment
import zio.ZLayer
import zio.stream.ZSink

import CommandExecutor.Execution

trait CommandExecutor:

  def apply(command: CommandDescriptor): Task[CommandExecution]

object CommandExecutor:

  def apply(
      crawlerManager: CrawlerManager,
      crawlerRepository: CrawlerRepository,
      timelineRepository: TimelineRepository
  ): CommandExecutor = wire[CommandExecutorImpl]

  object Execution:

    inline given (using descriptor: CommandDescriptor): Execution = new Execution(descriptor)

    def build()(using execution: Execution): CommandExecution =
      val currentTime = System.currentTimeMillis()
      CommandExecution(currentTime - execution.starttime, execution.descriptor)

  class Execution(
      val descriptor: CommandDescriptor,
      val starttime: Long = System.currentTimeMillis()
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

  private def addCrawler(descriptor: CrawlerDescriptor)(using Execution): Task[CommandExecution] =
    for _ <- crawlerRepository.add(descriptor)
    yield Execution.build()

  private def removeCrawler(name: String)(using Execution): Task[CommandExecution] =
    for _ <- crawlerRepository.remove(name)
    yield Execution.build()

  private def updateCrawler(name: String, descriptor: CrawlerDescriptor, shouldRunNow: Boolean)(
      using Execution
  ): Task[CommandExecution] =
    for _ <- crawlerRepository.update(descriptor)
    yield Execution.build()

  private def runCrawler(name: String)(using Execution): Task[CommandExecution] =
    val execution =
      for _ <- crawlerManager.run(name)
      yield Execution.build()

    execution.provideLayer(crawlerRepositoryLayer ++ timelineRepositoryLayer)

  private def runAll()(using Execution): Task[CommandExecution] =
    val execution =
      for _ <- crawlerManager.start().run(ZSink.collectAll)
      yield Execution.build()

    execution.provideLayer(crawlerRepositoryLayer ++ timelineRepositoryLayer)
