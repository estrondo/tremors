package graboid

import zio.Cause
import zio.UIO
import zio.ZIO

def logErrorCause[E](message: String)(cause: Cause[E]): UIO[Unit] =
  ZIO.logErrorCause(message, cause)

def logInfo[A](message: String)(value: A): UIO[Unit] =
  ZIO.logInfo(message)

def logDebug[A](message: String)(value: A): UIO[Unit] =
  ZIO.logDebug(message)
