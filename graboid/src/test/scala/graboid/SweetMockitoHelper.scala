package graboid

import testkit.core.SweetMockito
import zio.ZIO

trait SweetMockitoHelper:

  type OkT[T]    = ZIO[Any, Nothing, T]
  type ErrorF[E] = ZIO[Any, E, Nothing]

  given SweetMockito.OkF[OkT] = new SweetMockito.OkF:
    override def apply[A](a: A): ZIO[Any, Nothing, A] = ZIO.succeed(a)

  given SweetMockito.ErrorF[ErrorF] = new SweetMockito.ErrorF:
    override def apply[E](e: E): ZIO[Any, E, Nothing] = ZIO.fail(e)
