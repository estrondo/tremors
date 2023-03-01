package webapi.module

import com.softwaremill.macwire.wire
import zio.Task
import zkafka.KafkaManager
import zkafka.starter.KafkaConfig
import zkafka.starter.KafkaManagerStarter

object KafkaModule:

  def apply(config: KafkaConfig): Task[KafkaManager] =
    for manager <- KafkaManagerStarter(config, "webapi")
    yield manager
