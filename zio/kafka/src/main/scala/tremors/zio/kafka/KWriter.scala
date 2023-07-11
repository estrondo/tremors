package tremors.zio.kafka

import zio.Task

trait KWriter[-A]:

  def apply(value: A): Task[Array[Byte]]
