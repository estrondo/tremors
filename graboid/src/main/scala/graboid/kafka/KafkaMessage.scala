package graboid.kafka

case class KafkaMessage[A](value: A, key: Option[String], topic: String)
