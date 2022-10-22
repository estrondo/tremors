package tremors.graboid

import zio.{TaskLayer, Task}
import zio.kafka.producer.Producer
import zio.ZIO

trait KafkaModule:

  def producerLayer: TaskLayer[Producer]

object KafkaModule:

  def apply(): Task[KafkaModule] = ZIO.attempt {
    KafkaModuleImpl()
  }

private[graboid] class KafkaModuleImpl extends KafkaModule:

  override def producerLayer: TaskLayer[Producer] = throw IllegalStateException("produceLayer")
