package tremors.zio.http

import tremors.ExternalServiceException
import zio.ZIO
import zio.http.Response

trait ExceptionFactory[T]:

  def causedBy(message: String)(cause: Throwable): T

  def failed[R](message: String, response: Response): ZIO[R, T, Nothing]

object ExceptionFactory:

  given ExceptionFactory[ExternalServiceException] with

    override def failed[R](message: String, response: Response): ZIO[R, ExternalServiceException, Nothing] =
      getContentAsString(response, 8 * 1024)
        .mapError(causedBy(s"$message: It was impossible to read the content!"))
        .flatMap(content => ZIO.fail(ExternalServiceException(s"$message: $content")))

    override def causedBy(message: String)(cause: Throwable): ExternalServiceException =
      ExternalServiceException(message, cause)
