package webapi

object WebAPIException:

  class Invalid(message: String, cause: Throwable = null) extends WebAPIException(message, cause)

  class NotFound(message: String, cause: Throwable = null) extends WebAPIException(message, cause)

  class InvalidConfig(message: String, cause: Throwable = null) extends WebAPIException(message, cause)

sealed abstract class WebAPIException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)
