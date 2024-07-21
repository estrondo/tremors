package toph

abstract class TophException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause, false, false)

object TophException:

  class Security(message: String, cause: Throwable = null) extends TophException(message, cause)
