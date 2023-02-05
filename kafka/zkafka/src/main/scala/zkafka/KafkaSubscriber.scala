package zkafka

import zio.Task

trait KafkaSubscriber[A, B]:

  def accept(key: String, value: A): Task[Option[B]]
