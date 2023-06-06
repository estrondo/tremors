package graboid.command

import graboid.CrawlerExecutor
import graboid.CrawlingReport
import graboid.protocol.GraboidCommandResult
import graboid.protocol.GraboidCommandResult.Status
import graboid.protocol.RunAllPublishers
import zio.Task

trait RunAllPublishersExecutor extends GraboidCommandExecutor[RunAllPublishers]

class RunAllPublishersExecutorImpl(
    executor: CrawlerExecutor
) extends RunAllPublishersExecutor:

  override def execute(command: RunAllPublishers): Task[Status] =
    executor
      .run()
      .fold(reportFailure, reportSuccess)

  private def reportSuccess(report: CrawlingReport): Status =
    GraboidCommandResult.ok(
      "All publisher have been run.",
      "events"     -> report.events.toString(),
      "origins"    -> report.origins.toString(),
      "magnitudes" -> report.magnitudes.toString(),
      "failures"   -> report.failures.toString()
    )

  private def reportFailure(cause: Throwable): Status =
    GraboidCommandResult.failed("It was impossible to run all publishers.", cause)
