package tremors.graboid

object GraboidException:

  case class NotFound(message: String, cause: Throwable = null)
      extends GraboidException(message, cause)

sealed abstract class GraboidException(message: String = null, cause: Throwable = null)
    extends RuntimeException(message, cause)
