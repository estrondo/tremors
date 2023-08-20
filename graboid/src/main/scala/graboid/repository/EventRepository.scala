package graboid.repository

import com.softwaremill.macwire.wire
import tremors.zio.farango.CollectionManager

trait EventRepository

object EventRepository:

  def apply(collectionManager: CollectionManager): EventRepository =
    wire[Impl]

private class Impl(collectionManager: CollectionManager) extends EventRepository
