package graboid.repository

import com.softwaremill.macwire.wire
import one.estrondo.farango.Collection

trait EventRepository

object EventRepository:

  def apply(collection: Collection): EventRepository =
    wire[Impl]

private class Impl(collection: Collection) extends EventRepository
