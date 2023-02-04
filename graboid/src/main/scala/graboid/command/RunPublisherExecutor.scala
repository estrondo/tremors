package graboid.command

import graboid.protocol.RunPublisher
import graboid.protocol.GraboidCommandResult.Status
import zio.Task
import graboid.CrawlerExecutor
import graboid.protocol.GraboidCommandResult

trait RunPublisherExecutor extends GraboidCommandExecutor[RunPublisher]

class RunPublisherExecutorImpl(
    crawlerExecutor: CrawlerExecutor
) extends RunPublisherExecutor:

  override def execute(command: RunPublisher): Task[Status] =
    for report <- crawlerExecutor.runPublisher(command.publisherKey)
    yield GraboidCommandResult.Ok(
      s"Run publisher=${command.publisherKey}: events=${report.events}, origins=${report.origins}, magnitudes=${report.magnitudes} and failures=${report.failures}."
    )
