package ziorango

import com.arangodb.ArangoCursor
import farango.FarangoStream
import zio.stream.ZStream
import ziorango.Ziorango.S

given FarangoStream[Ziorango.S] = ZFarangoStream
import scala.util.Try
import scala.util.Success
import zio.ZIO
import scala.util.Failure
import java.util.stream

object ZFarangoStream extends FarangoStream[Ziorango.S]:

  override def mapFromJavaStream[A, B](stream: () => JavaStream[A])(fn: A => B): S[B] =
    ZStream
      .fromJavaStream(stream())
      .map(fn)
