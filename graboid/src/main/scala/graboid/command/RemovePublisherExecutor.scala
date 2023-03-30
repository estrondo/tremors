package graboid.command

import graboid.CrawlerExecutor
import graboid.PublisherManager
import graboid.protocol.GraboidCommandResult
import graboid.protocol.GraboidCommandResult.Status
import graboid.protocol.RemovePublisher
import zio.Cause
import zio.Task
import zio.ZIO

trait RemovePublisherExecutor extends GraboidCommandExecutor[RemovePublisher]

class RemovePublisherExecutorImpl(
    publisherManager: PublisherManager,
    crawlerExecutor: CrawlerExecutor
) extends RemovePublisherExecutor:

  override def execute(command: RemovePublisher): Task[Status] =
    for
      result <- publisherManager.remove(command.publisherKey)
      count  <- if result.isDefined then removeExecutions(command)
                else ZIO.succeed(0L)
    yield GraboidCommandResult.ok(
      "Publisher has removed.",
      "publisherKey" -> command.publisherKey,
      "executions"   -> count.toString
    )

  private def removeExecutions(command: RemovePublisher): Task[Long] =
    crawlerExecutor
      .removeExecutions(command.publisherKey)
      .catchAll(cause =>
        ZIO.logWarningCause("It was impossible to remove all releated executions.", Cause.die(cause)) as 0L
      )
