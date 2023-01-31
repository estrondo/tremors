package webapi1x

import com.softwaremill.macwire.wire
import webapi1x.WebApi.WebApiConfig
import zio.Scope
import zio.Task
import zio.URLayer
import zio.ZIO
import zio.ZLayer
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings

trait KafkaModule:

  def producerLayer: URLayer[Scope, Producer]

object KafkaModule:

  def apply(config: WebApiConfig): Task[KafkaModule] = ZIO.attempt(wire[Impl])

  val ClientID = s"${BuildInfo.name}:${BuildInfo.version}"

  private class Impl(config: WebApiConfig) extends KafkaModule:

    private def kafkaConfig = config.kafka.producer

    override val producerLayer: URLayer[Scope, Producer] =
      ZLayer(
        Producer
          .make(
            ProducerSettings(bootstrapServers)
              .withClientId(ClientID)
          )
          .orDie
      )

    private def bootstrapServers: List[String] = ???
