package tremors.zio.kafka

import zio.Task

trait KReader[+A]:

  def apply(bytes: Array[Byte]): Task[A]
