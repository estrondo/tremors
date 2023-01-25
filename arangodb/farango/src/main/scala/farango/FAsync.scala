package farango

import java.util.concurrent.CompletionStage

object FAsync:

  transparent inline def apply[F[_]: FAsync]: FAsync[F] = summon[FAsync[F]]

trait FAsync[F[_]]:

  def map[A, B](a: F[A])(fn: A => B): F[B]

  def mapFromCompletionStage[A, B](a: => CompletionStage[A])(fn: A => B): F[B]

  def flatMap[A, B](a: F[A])(fn: A => F[B]): F[B]

  def flatMapFromCompletionStage[A, B](a: => CompletionStage[A])(fn: A => F[B]): F[B]

  def succeed[A](a: => A): F[A]

  def none[A]: F[Option[A]]
