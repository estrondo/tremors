package toph.module

import com.softwaremill.macwire.wire
import zio.Task
import zkafka.KafkaManager
import zkafka.starter.KafkaConfig
import zkafka.starter.KafkaManagerStarter

trait KafkaModule:

  val manager: KafkaManager

object KafkaModule:

  def apply(config: KafkaConfig): Task[KafkaModule] =
    for manager <- KafkaManagerStarter(config, "toph")
    yield wire[Impl]

  private class Impl(override val manager: KafkaManager) extends KafkaModule
