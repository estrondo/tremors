package webapi1x

import scala.util.Try
import zio.{Task, ZIO}

extension [T](value: Try[T]) def toZIO(): Task[T] = ZIO.fromTry(value)
