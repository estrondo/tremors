package graboid

abstract class GraboidException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)
object GraboidException:

  class QuakeMLException(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class Command(message: String, cause: Throwable = null) extends GraboidException(message, cause)
