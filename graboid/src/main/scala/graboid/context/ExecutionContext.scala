package graboid.context

case class ExecutionContext(owner: Owner)

object ExecutionContext:

  def command(): ExecutionContext = ExecutionContext(Owner.Command)

  def scheduler(): ExecutionContext = ExecutionContext(Owner.Scheduler)
