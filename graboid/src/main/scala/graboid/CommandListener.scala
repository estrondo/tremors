package graboid

import graboid.protocol.GraboidCommand
import graboid.protocol.GraboidCommandExecution
import io.bullet.borer.Cbor
import zio.Task
import zio.ZIO
import zio.kafka.consumer.CommittableRecord
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.Subscription
import zio.kafka.serde.Serde
import zio.stream.ZStream

object CommandListener:

  val Topic = "tremors.graboid-command"
  val Group = "graboid"

class CommandListener(executor: CommandExecutor):

  def run(): ZStream[Consumer, Throwable, GraboidCommandExecution] =
    Consumer
      .subscribeAnd(Subscription.topics(CommandListener.Topic))
      .plainStream(Serde.string, Serde.byteArray)
      .mapZIO(readRecord)
      .tap(descriptor => ZIO.logInfo(s"It has received a new command: $descriptor"))
      .mapZIO(executor.apply)

  private def readRecord(record: CommittableRecord[String, Array[Byte]]): Task[GraboidCommand] =
    ZIO.fromTry(Cbor.decode(record.value).to[GraboidCommand].valueTry)