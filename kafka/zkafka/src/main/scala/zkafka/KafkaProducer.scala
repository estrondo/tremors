package zkafka

import zio.Task

trait KafkaProducer[A, B]:

  def accept(key: String, value: A): Task[Seq[KafkaMessage[B]]]
