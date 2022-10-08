package farango

import com.arangodb.ArangoCursor

import scala.util.Try

trait FApplicativeStream[S[_]]:

  type JavaStream[T] = java.util.stream.Stream[T]

  def mapFromJavaStream[A, B](stream: () => JavaStream[A])(fn: A => B): S[B]
