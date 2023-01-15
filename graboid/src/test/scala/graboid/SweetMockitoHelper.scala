package graboid

import org.mockito.stubbing.OngoingStubbing
import testkit.core.SweetMockito
import zio.Tag
import zio.URIO
import zio.ZIO

import java.net.URI

trait SweetMockitoHelper:

  type OkT[T]    = ZIO[Any, Nothing, T]
  type ErrorF[E] = ZIO[Any, E, Nothing]

  class SweetMock[M: Tag]:

    def returnF[T](call: M => T)(value: => T): URIO[M, OngoingStubbing[Any]] =
      for
        mock   <- ZIO.service[M]
        ongoing = SweetMockito.returnF(call(mock))(value)
      yield ongoing

    def failF[E](call: M => Any)(error: => E): URIO[M, OngoingStubbing[Any]] =
      for
        mock   <- ZIO.service[M]
        ongoing = SweetMockito.failF(call(mock))(error)
      yield ongoing

  given SweetMockito.OkF[OkT] = new SweetMockito.OkF:
    override def apply[A](a: A): ZIO[Any, Nothing, A] = ZIO.succeed(a)

  given SweetMockito.ErrorF[ErrorF] = new SweetMockito.ErrorF:
    override def apply[E](e: E): ZIO[Any, E, Nothing] = ZIO.fail(e)

  def sweetMock[M: Tag] = SweetMock[M]
