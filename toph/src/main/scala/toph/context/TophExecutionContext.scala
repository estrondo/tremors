package toph.context

import scala.reflect.ClassTag
import toph.model.AuthenticatedUser

case class TophExecutionContext(owner: Owner)

object TophExecutionContext:

  inline def apply()(using inline executionContext: TophExecutionContext): TophExecutionContext = executionContext

  def identifiedUser(id: String): TophExecutionContext =
    new TophExecutionContext(owner = Owner.IdentifiedUser(id))

  def identifiedUser(authenticatedUser: AuthenticatedUser): TophExecutionContext =
    new TophExecutionContext(Owner.IdentifiedUser(authenticatedUser.claims.id))

  def systemUser(name: String): TophExecutionContext =
    new TophExecutionContext(owner = Owner.SystemUser(name))

  def systemUser[T: ClassTag]: TophExecutionContext =
    new TophExecutionContext(owner = Owner.SystemUser(summon[ClassTag[T]].runtimeClass.getCanonicalName))
