package tremors

class ExternalServiceException(message: String, cause: Throwable = null) extends TremorsException(message, cause)
