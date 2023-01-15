package graboid

import scala.util.control.NoStackTrace

object GraboidException:

  class NotFound(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class Unexpected(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class Invalid(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class IllegalResponse(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class IllegalRequest(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class CrawlerException(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class IllegalState(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class MultipleCause(message: String, causes: Seq[Throwable]) extends GraboidException(message)

  def illegalRequest(message: String)(cause: Throwable = null) = IllegalRequest(message, cause)

  def unexpected(message: String)(cause: Throwable) = Unexpected(message, cause)

sealed abstract class GraboidException(message: String = null, cause: Throwable = null)
    extends RuntimeException(message, cause, true, false)
