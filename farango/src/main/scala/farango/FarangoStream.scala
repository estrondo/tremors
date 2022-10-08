package farango

import com.arangodb.ArangoCursor

import scala.util.Try

trait FarangoStream[S[_]]:

  type JavaStream[T] = java.util.stream.Stream[T]

  def mapFromJavaStream[A, B](stream: () => JavaStream[A])(fn: A => B): S[B]
