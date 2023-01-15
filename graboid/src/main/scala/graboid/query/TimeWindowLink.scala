package graboid.query

sealed trait TimeWindowLink

object TimeWindowLink:

  case class With(key: String) extends TimeWindowLink
  case object Unliked          extends TimeWindowLink
