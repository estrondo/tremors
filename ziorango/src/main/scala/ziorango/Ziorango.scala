package ziorango

import zio.ZIO
import com.arangodb.async.ArangoDatabaseAsync
import zio.stream.ZStream.apply
import zio.stream.ZStream

object Ziorango:

  type F[T] = ZIO[Any, Throwable, T]
  type S[T] = ZStream[Any, Throwable, T]
