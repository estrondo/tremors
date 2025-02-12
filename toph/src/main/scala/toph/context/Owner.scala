package toph.context

sealed trait Owner

object Owner:

  case class Account(key: String) extends Owner

  case class System(name: String) extends Owner
