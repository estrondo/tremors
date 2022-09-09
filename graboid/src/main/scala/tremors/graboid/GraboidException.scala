package tremors.graboid

object GraboidException:

  class NotFound(message: String, cause: Throwable = null)
      extends GraboidException(message, cause)

  class Unexpected(message: String, cause: Throwable = null)
      extends GraboidException(message, cause)

  class IllegalResponse(message: String, cause: Throwable = null)
      extends GraboidException(message, cause)

  class IllegalRequest(message: String, cause: Throwable = null)
      extends GraboidException(message, cause)

  class CrawlerException(message: String, cause: Throwable = null)
      extends GraboidException(message, cause)

sealed abstract class GraboidException(message: String = null, cause: Throwable = null)
    extends RuntimeException(message, cause)
