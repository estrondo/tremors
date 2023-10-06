package graboid.context

sealed trait Owner

object Owner:

  case object Scheduler extends Owner
  case object Command   extends Owner
