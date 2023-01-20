package graboid

import graboid.kafka.KafkaProducer
import graboid.protocol.GraboidCommandResult
import graboid.kafka.KafkaMessage
import graboid.kafka.GraboidCommandResultTopic
import zio.Task
import zio.ZIO

trait CommandResultPublisher extends KafkaProducer[GraboidCommandResult, GraboidCommandResult]

object CommandResultPublisher:

  private class CommandResultPublisherImpl extends CommandResultPublisher:

    def accept(key: String, result: GraboidCommandResult): Task[Seq[KafkaMessage[GraboidCommandResult]]] = ZIO.succeed {
      Seq(KafkaMessage(result, None, GraboidCommandResultTopic))
    }
