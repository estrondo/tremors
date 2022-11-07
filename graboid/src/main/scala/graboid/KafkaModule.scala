package graboid

import zio.Task
import zio.TaskLayer
import zio.ZIO
import zio.kafka.producer.Producer

trait KafkaModule:

  def producerLayer: TaskLayer[Producer]

object KafkaModule:

  def apply(): Task[KafkaModule] = ZIO.attempt {
    KafkaModuleImpl()
  }

private[graboid] class KafkaModuleImpl extends KafkaModule:

  override def producerLayer: TaskLayer[Producer] = throw IllegalStateException("produceLayer")
