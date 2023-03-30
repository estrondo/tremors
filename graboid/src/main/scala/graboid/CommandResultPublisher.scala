package graboid

import graboid.kafka.GraboidCommandResultTopic
import graboid.protocol.GraboidCommandResult
import zio.Task
import zio.ZIO
import zkafka.KafkaMessage
import zkafka.KafkaProducer

trait CommandResultPublisher extends KafkaProducer[GraboidCommandResult, GraboidCommandResult]

object CommandResultPublisher:

  def apply(): CommandResultPublisher = new Impl()

  private class Impl extends CommandResultPublisher:

    def accept(key: String, result: GraboidCommandResult): Task[Seq[KafkaMessage[GraboidCommandResult]]] = ZIO.succeed {
      Seq(KafkaMessage(result, None, GraboidCommandResultTopic))
    }
