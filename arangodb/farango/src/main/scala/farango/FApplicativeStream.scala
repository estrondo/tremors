package farango

import com.arangodb.ArangoCursor

import scala.util.Try
import java.util.concurrent.CompletionStage

object FApplicativeStream:

  transparent inline def apply[S[_]: FApplicativeStream]: FApplicativeStream[S] =
    summon[FApplicativeStream[S]]

trait FApplicativeStream[S[_]]:

  type JavaStream[T] = java.util.stream.Stream[T]

  def mapFromJavaStream[A, B](stream: => JavaStream[A])(fn: A => B): S[B]

  def mapFromCompletionStage[A, B](stream: => CompletionStage[JavaStream[A]])(fn: A => B): S[B]
