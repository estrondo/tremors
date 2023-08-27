package tremors.zio.http

import zio.ZIO
import zio.http.Response

object HttpChecker:

  def apply[T <: Throwable]: PartialApply[T] = PartialApply[T]

  class PartialApply[T <: Throwable]:

    def apply[R](message: String, response: ZIO[R, ? <: Throwable, Response])(using
        ExceptionFactory[T]
    ): ZIO[R, T, Response] =
      response
        .mapError(summon[ExceptionFactory[T]].causedBy("Unhandled exception!"))
        .flatMap { response =>
          if response.status.isSuccess then ZIO.succeed(response)
          else summon[ExceptionFactory[T]].failed(message, response)
        }
