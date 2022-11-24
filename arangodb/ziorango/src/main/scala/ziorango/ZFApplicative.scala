package ziorango

import farango.FApplicative
import zio.ZIO

import java.util.concurrent.CompletionStage
import ziorango.Ziorango.F

given FApplicative[Ziorango.F] = ZFApplicative

object ZFApplicative extends FApplicative[Ziorango.F]:

  override def mapFromCompletionStage[A, B](
      completionStage: => CompletionStage[A]
  )(fn: A => B): Ziorango.F[B] =
    for a <- ZIO.fromCompletionStage(completionStage)
    yield fn(a)

  override def map[A, B](a: Ziorango.F[A])(fn: A => B): Ziorango.F[B] =
    for va <- a
    yield fn(va)

  override def flatMap[A, B](a: Ziorango.F[A])(fn: A => Ziorango.F[B]): Ziorango.F[B] =
    a.flatMap(fn)

  override def flatMapFromCompletionStage[A, B](
      completionStage: => CompletionStage[A]
  )(fn: A => Ziorango.F[B]): Ziorango.F[B] =
    ZIO
      .fromCompletionStage(completionStage)
      .flatMap(a => fn(a))

  override def pure[A](a: A): Ziorango.F[A] = ZIO.succeed(a)
