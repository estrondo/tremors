package toph.context

import scala.reflect.ClassTag
import toph.model.Account

case class TophExecutionContext(owner: Owner)

object TophExecutionContext:

  inline def apply()(using inline executionContext: TophExecutionContext): TophExecutionContext = executionContext

  def account(account: Account): TophExecutionContext =
    new TophExecutionContext(Owner.Account(account.key))

  def system[T: ClassTag]: TophExecutionContext =
    new TophExecutionContext(owner = Owner.System(summon[ClassTag[T]].runtimeClass.getCanonicalName))
