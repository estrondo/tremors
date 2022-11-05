package graboid

import ziotestcontainers.*
import com.dimafeng.testcontainers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import zio.RIO
import zio.ZIO
import zio.ZLayer
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.TaskLayer
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings

object KafkaContainerLayer:

  val layer = layerOf {
    KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.2"))
  }

  def createConsumerLayer(
      groupId: String,
      offsetReset: String = "earliest"
  ): RIO[KafkaContainer, TaskLayer[Consumer]] =
    ZIO.serviceWith(container =>
      ZLayer.scoped(
        Consumer.make(
          ConsumerSettings(List(container.bootstrapServers))
            .withGroupId(groupId)
            .withProperty("auto.offset.reset", offsetReset)
        )
      )
    )

  def createProducerLayer(): RIO[KafkaContainer, TaskLayer[Producer]] =
    ZIO.serviceWith(container =>
      ZLayer.scoped(Producer.make(ProducerSettings(List(container.bootstrapServers))))
    )
