package zkafka.starter

import zio.Task
import zio.ZIO
import zkafka.KafkaManager
import zio.kafka.producer.ProducerSettings
import zio.kafka.consumer.ConsumerSettings

object KafkaManagerStarter:

  def apply(config: KafkaConfig, groupIdDefault: String): Task[KafkaManager] =
    val producerSettings = ProducerSettings(config.bootstrap)
      .withCloseTimeout(config.closeTimeout)
      .withClientId(config.clientId)

    val consumerSettings = ConsumerSettings(config.bootstrap)
      .withClientId(config.clientId)
      .withGroupId(config.group.getOrElse(groupIdDefault))
      .withCloseTimeout(config.closeTimeout)

    ZIO.attempt(KafkaManager(consumerSettings, producerSettings))
