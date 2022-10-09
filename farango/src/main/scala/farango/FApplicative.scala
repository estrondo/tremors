package farango

import java.util.concurrent.CompletionStage

trait FApplicative[F[_]]:

  def map[A, B](a: F[A])(fn: A => B): F[B]

  def mapFromCompletionStage[A, B](a: CompletionStage[A])(fn: A => B): F[B]

  def flatMap[A, B](a: F[A])(fn: A => F[B]): F[B]

  def flatMapFromCompletionStage[A, B](a: CompletionStage[A])(fn: A => F[B]): F[B]

  def pure[A](a: A): F[A]
