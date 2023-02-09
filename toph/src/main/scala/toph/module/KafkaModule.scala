package toph.module

import com.softwaremill.macwire.wire
import zio.Task
import zkafka.KafkaManager
import zkafka.starter.KafkaConfig
import zkafka.starter.KafkaManagerStarter

trait KafkaModule

object KafkaModule:

  def apply(config: KafkaConfig): Task[KafkaModule] =
    for kafkaManager <- KafkaManagerStarter(config, "toph")
    yield wire[Impl]

  private class Impl(kafkaManager: KafkaManager) extends KafkaModule
