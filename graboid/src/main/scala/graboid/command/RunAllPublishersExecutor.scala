package graboid.command

import graboid.protocol.RunAllPublishers
import graboid.protocol.GraboidCommandResult.Status
import zio.Task
import zio.ZIO
import zio.UIO
import graboid.PublisherManager
import graboid.CrawlerExecutor
import graboid.protocol.GraboidCommandResult
import graboid.CrawlingReport

trait RunAllPublishersExecutor extends GraboidCommandExecutor[RunAllPublishers]

class RunAllPublishersExecutorImpl(
    executor: CrawlerExecutor
) extends RunAllPublishersExecutor:

  override def execute(command: RunAllPublishers): Task[Status] =
    executor
      .run()
      .fold(reportFailure, reportSuccess)

  private def reportSuccess(report: CrawlingReport): Status =
    GraboidCommandResult.Ok(
      s"There were detected: events=${report.events}, origins=${report.origins}, magnitudes=${report.magnitudes} and failures=${report.failures}."
    )

  private def reportFailure(cause: Throwable): Status =
    GraboidCommandResult.failed("It was impossible to run all publishers.", cause)
