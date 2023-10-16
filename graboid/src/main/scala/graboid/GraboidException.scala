package graboid

import tremors.TremorsException

abstract class GraboidException(message: String, cause: Throwable = null) extends TremorsException(message, cause)

object GraboidException:

  class QuakeML(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class Command(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class Crawling(message: String, cause: Throwable = null) extends GraboidException(message, cause)
