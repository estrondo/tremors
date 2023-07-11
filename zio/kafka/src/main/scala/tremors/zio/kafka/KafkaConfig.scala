package tremors.zio.kafka

final case class KafkaConfig(
    consumer: KafkaConsumerConfig,
    producer: KafkaProducerConfig
)

final case class KafkaConsumerConfig(
    groupId: String,
    bootstrapServers: List[String]
)

final case class KafkaProducerConfig(
    bootstrapServers: List[String]
)
