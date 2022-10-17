package tremors.graboid

import zio.TaskLayer
import zio.kafka.producer.Producer

trait KafkaModule:

  def producerLayer: TaskLayer[Producer]
