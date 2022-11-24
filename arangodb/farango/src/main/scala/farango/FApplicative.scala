package farango

import java.util.concurrent.CompletionStage

object FApplicative:

  transparent inline def apply[F[_]: FApplicative]: FApplicative[F] = summon[FApplicative[F]]

trait FApplicative[F[_]]:

  def map[A, B](a: F[A])(fn: A => B): F[B]

  def mapFromCompletionStage[A, B](a: => CompletionStage[A])(fn: A => B): F[B]

  def flatMap[A, B](a: F[A])(fn: A => F[B]): F[B]

  def flatMapFromCompletionStage[A, B](a: => CompletionStage[A])(fn: A => F[B]): F[B]

  def pure[A](a: A): F[A]
