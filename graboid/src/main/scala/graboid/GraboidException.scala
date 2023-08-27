package graboid

import tremors.TremorsException

abstract class GraboidException(message: String, cause: Throwable = null) extends TremorsException(message, cause)

object GraboidException:

  class QuakeMLException(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class Command(message: String, cause: Throwable = null) extends GraboidException(message, cause)

  class CrawlingException(message: String, cause: Throwable = null) extends GraboidException(message, cause)
