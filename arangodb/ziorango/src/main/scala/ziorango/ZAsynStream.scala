package ziorango

import farango.FAsyncStream
import zio.stream.ZStream
import java.util.concurrent.CompletionStage
import ziorango.S
import java.util.stream
import scala.jdk.FutureConverters.CompletionStageOps.given
import zio.ZIO

given ZAsyncStream: FAsyncStream[S] = new FAsyncStream[S]:

  override def mapFromJavaStream[A, B](stream: => JavaStream[A])(fn: A => B): S[B] =
    ZStream
      .fromJavaStream(stream)
      .map(fn)

  override def mapFromCompletionStage[A, B](stream: => CompletionStage[JavaStream[A]])(
      fn: A => B
  ): S[B] =
    for
      javaStream <- ZStream.fromZIO(ZIO.fromCompletionStage(stream))
      element    <- ZStream.fromJavaStream(javaStream)
    yield fn(element)
