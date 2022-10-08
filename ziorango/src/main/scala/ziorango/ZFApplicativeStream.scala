package ziorango

import farango.FApplicativeStream
import zio.stream.ZStream

given FApplicativeStream[Ziorango.S] = ZFApplicativeStream

object ZFApplicativeStream extends FApplicativeStream[Ziorango.S]:

  override def mapFromJavaStream[A, B](stream: () => JavaStream[A])(fn: A => B): Ziorango.S[B] =
    ZStream
      .fromJavaStream(stream())
      .map(fn)
