package graboid.command

import graboid.CrawlerExecutor
import graboid.protocol.GraboidCommandResult
import graboid.protocol.GraboidCommandResult.Status
import graboid.protocol.RunPublisher
import zio.Task

trait RunPublisherExecutor extends GraboidCommandExecutor[RunPublisher]

class RunPublisherExecutorImpl(
    crawlerExecutor: CrawlerExecutor
) extends RunPublisherExecutor:

  override def execute(command: RunPublisher): Task[Status] =
    for report <- crawlerExecutor.runPublisher(command.publisherKey)
    yield GraboidCommandResult.ok(
      "Publisher has been run.",
      "events"     -> report.events.toString(),
      "origins"    -> report.origins.toString(),
      "magnitudes" -> report.magnitudes.toString(),
      "failures"   -> report.failures.toString()
    )
