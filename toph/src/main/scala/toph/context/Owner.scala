package toph.context

sealed trait Owner:
  def key: String

object Owner:

  case class Account(account: toph.model.Account) extends Owner:
    override def key = account.key

  case class System(key: String) extends Owner
