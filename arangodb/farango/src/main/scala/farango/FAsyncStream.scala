package farango

import com.arangodb.ArangoCursor

import java.util.concurrent.CompletionStage
import scala.util.Try

object FAsyncStream:

  transparent inline def apply[S[_]: FAsyncStream]: FAsyncStream[S] =
    summon[FAsyncStream[S]]

trait FAsyncStream[S[_]]:

  type JavaStream[T] = java.util.stream.Stream[T]

  def mapFromJavaStream[A, B](stream: => JavaStream[A])(fn: A => B): S[B]

  def mapFromCompletionStage[A, B](stream: => CompletionStage[JavaStream[A]])(fn: A => B): S[B]
