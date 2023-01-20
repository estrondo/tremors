package graboid.layer

import testkit.zio.testcontainers.KafkaContainerLayer
import graboid.kafka.KafkaManager
import graboid.kafka.GraboidCommandTopic

val ConsumerLayer = KafkaContainerLayer
  .createConsumerLayer2(groupId = GraboidCommandTopic)

val ProducerLayer = KafkaContainerLayer.producerLayer

val KafkaLayer = KafkaContainerLayer.layer ++ (KafkaContainerLayer.layer >>> (ConsumerLayer ++ ProducerLayer))
