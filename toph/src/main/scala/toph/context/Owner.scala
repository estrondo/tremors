package toph.context

sealed trait Owner

object Owner:

  case class IdentifiedUser(id: String) extends Owner

  case class SystemUser(name: String) extends Owner
