package toph.context

import scala.reflect.ClassTag
import toph.model.Account

case class TophExecutionContext(owner: Owner)

object TophExecutionContext:

  inline def apply()(using inline executionContext: TophExecutionContext): TophExecutionContext = executionContext

  def identifiedAccount(id: String): TophExecutionContext =
    new TophExecutionContext(owner = Owner.IdentifiedAccount(id))

  def identifiedAccount(account: Account): TophExecutionContext =
    new TophExecutionContext(Owner.IdentifiedAccount(account.key))

  def systemUser(name: String): TophExecutionContext =
    new TophExecutionContext(owner = Owner.SystemUser(name))

  def systemUser[T: ClassTag]: TophExecutionContext =
    new TophExecutionContext(owner = Owner.SystemUser(summon[ClassTag[T]].runtimeClass.getCanonicalName))
