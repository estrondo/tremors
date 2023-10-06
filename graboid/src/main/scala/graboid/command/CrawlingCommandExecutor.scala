package graboid.command

import com.softwaremill.macwire.wire
import graboid.context.ExecutionContext
import graboid.crawling.CrawlingExecutor
import graboid.crawling.EventCrawlingQuery
import graboid.manager.DataCentreManager
import graboid.protocol.CrawlingCommand
import graboid.protocol.RunDataCentreEventCrawling
import graboid.protocol.RunEventCrawling
import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.http.Client
import zio.kafka.producer.Producer

trait CrawlingCommandExecutor:

  def apply(command: CrawlingCommand): Task[CrawlingCommand]

object CrawlingCommandExecutor:

  def apply(
      executor: CrawlingExecutor,
      dataCentreManager: DataCentreManager,
      layer: TaskLayer[Client & Producer]
  ): Task[CrawlingCommandExecutor] =
    ZIO.succeed(wire[Impl])

  private class Impl(
      executor: CrawlingExecutor,
      dataCentreManager: DataCentreManager,
      layer: TaskLayer[Client & Producer]
  ) extends CrawlingCommandExecutor:

    override def apply(command: CrawlingCommand): Task[CrawlingCommand] =
      command match
        case command: RunEventCrawling =>
          for
            query  <- convertToQuery(command)
            result <- executor.execute(query)(using ExecutionContext.command()).runDrain.provideLayer(layer)
          yield command

        case command: RunDataCentreEventCrawling =>
          for
            query <- convertToQuery(command)
            opt   <- dataCentreManager.get(command.dataCentre)
            _     <- opt match
                       case Some(dataCentre) =>
                         executor.execute(dataCentre, query)(using ExecutionContext.command()).runDrain.provideLayer(layer)
                       case _                => ZIO.unit
          yield command

    private def convertToQuery(command: RunEventCrawling) =
      ZIO.succeed(
        EventCrawlingQuery(
          starting = command.starting,
          ending = command.ending,
          timeWindow = command.timeWindow,
          queries = Seq(
            EventCrawlingQuery.Query(
              magnitudeType = command.magnitudeType,
              eventType = command.eventType,
              min = command.minMagnitude,
              max = command.maxMagnitude
            )
          )
        )
      )

    private def convertToQuery(command: RunDataCentreEventCrawling) =
      ZIO.succeed(
        EventCrawlingQuery(
          starting = command.starting,
          ending = command.ending,
          timeWindow = command.timeWindow,
          queries = Seq(
            EventCrawlingQuery.Query(
              magnitudeType = command.magnitudeType,
              eventType = command.eventType,
              min = command.minMagnitude,
              max = command.maxMagnitude
            )
          )
        )
      )
