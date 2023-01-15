package testkit.core

import scala.reflect.ClassTag
import org.mockito.Mockito
import org.mockito.Mock
import org.mockito.stubbing.OngoingStubbing
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import org.mockito.ArgumentMatchers

object SweetMockito:

  trait OkF[F[_]]:

    def apply[A](a: A): F[A]

  trait ErrorF[F[_]]:

    def apply[E](e: E): F[E]

  def any[T: ClassTag] =
    ArgumentMatchers.any(summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])

  def eqTo[T: ClassTag](value: T) =
    ArgumentMatchers.eq(value)

  def apply[T: ClassTag]: T = Mockito.mock(summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])

  def returnF[T, F[_]: OkF](mock: => T)(value: T): OngoingStubbing[Any] =
    Mockito.when(mock).thenReturn(summon[OkF[F]](value))

  def failF[E, F[_]: ErrorF](mock: => Any)(error: E): OngoingStubbing[Any] =
    Mockito.when(mock).thenReturn(summon[ErrorF[F]](error))

  def answerF[T, F[_]: OkF](mock: => T)(fn: InvocationOnMock => T): OngoingStubbing[Any] =
    Mockito.when(mock).thenAnswer { invocation =>
      summon[OkF[F]](fn(invocation))
    }
