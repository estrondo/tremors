package tremors.zio.kafka

import zio.stream.ZStream

case class KConsumer[-A, +B](topic: String, consumer: (String, A) => ZStream[Any, Throwable, B])
