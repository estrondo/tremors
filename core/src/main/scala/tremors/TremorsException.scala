package tremors

abstract class TremorsException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause, true, false)
