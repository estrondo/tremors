package ziorango

import farango.FAsync
import zio.ZIO
import ziorango.F

import java.util.concurrent.CompletionStage

given ZAsync: FAsync[F] = new FAsync[F]:

  override def mapFromCompletionStage[A, B](
      completionStage: => CompletionStage[A]
  )(fn: A => B): F[B] =
    for a <- ZIO.fromCompletionStage(completionStage)
    yield fn(a)

  override def map[A, B](a: F[A])(fn: A => B): F[B] =
    for va <- a
    yield fn(va)

  override def flatMap[A, B](a: F[A])(fn: A => F[B]): F[B] =
    a.flatMap(fn)

  override def flatMapFromCompletionStage[A, B](
      completionStage: => CompletionStage[A]
  )(fn: A => F[B]): F[B] =
    ZIO
      .fromCompletionStage(completionStage)
      .flatMap(a => fn(a))

  override def succeed[A](a: => A): F[A] = ZIO.succeed(a)

  override def none[A]: F[Option[A]] = ZIO.none
