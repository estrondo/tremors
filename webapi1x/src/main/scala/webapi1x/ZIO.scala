package webapi1x

import zio.Task
import zio.ZIO

import scala.util.Try

extension [T](value: Try[T]) def toZIO(): Task[T] = ZIO.fromTry(value)
