package graboid.module

import graboid.command.CommandExecutor
import graboid.command.CrawlingCommandExecutor
import graboid.command.DataCentreCommandExecutor
import zio.Task
import zio.TaskLayer
import zio.http.Client
import zio.kafka.producer.Producer

trait CommandModule:

  def commandExecutor: CommandExecutor

object CommandModule:

  def apply(
      managerModule: ManagerModule,
      crawlingModule: CrawlingModule,
      layer: TaskLayer[Client & Producer],
  ): Task[CommandModule] =
    for
      dataCentreCommandExecutor <- DataCentreCommandExecutor(managerModule.dataCentreManager)
      crawlingCommandExecutor   <- CrawlingCommandExecutor
                                     .apply(crawlingModule.crawlingExecutor, managerModule.dataCentreManager, layer)
      commandExecutor           <- CommandExecutor(dataCentreCommandExecutor, crawlingCommandExecutor)
    yield new Impl(commandExecutor)

  private class Impl(
      val commandExecutor: CommandExecutor,
  ) extends CommandModule
