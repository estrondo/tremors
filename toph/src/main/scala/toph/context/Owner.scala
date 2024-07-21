package toph.context

sealed trait Owner

object Owner:

  case class IdentifiedAccount(key: String) extends Owner

  case class SystemUser(name: String) extends Owner
