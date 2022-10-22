package farango

import com.arangodb.ArangoCursor

import scala.util.Try
import java.util.concurrent.CompletionStage

trait FApplicativeStream[S[_]]:

  type JavaStream[T] = java.util.stream.Stream[T]

  def mapFromJavaStream[A, B](stream: => JavaStream[A])(fn: A => B): S[B]

  def mapFromCompletionStage[A, B](stream: => CompletionStage[JavaStream[A]])(fn: A => B): S[B]
