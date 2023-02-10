package zkafka.starter

import zio.Task
import zio.ZIO
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.producer.ProducerSettings
import zkafka.KafkaManager

object KafkaManagerStarter:

  def apply(config: KafkaConfig, groupIdDefault: String): Task[KafkaManager] =
    val producerSettings = ProducerSettings(config.bootstrap.toList)
      .withCloseTimeout(config.closeTimeout)
      .withClientId(config.clientId)

    val consumerSettings = ConsumerSettings(config.bootstrap.toList)
      .withClientId(config.clientId)
      .withGroupId(config.group.getOrElse(groupIdDefault))
      .withCloseTimeout(config.closeTimeout)

    ZIO.attempt(KafkaManager(consumerSettings, producerSettings))
