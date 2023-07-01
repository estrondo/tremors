package graboid.repository

import com.softwaremill.macwire.wire
import one.estrondo.farango.Collection

trait HypocentreRepository

object HypocentreRepository:

  def apply(collection: Collection): HypocentreRepository =
    wire[Impl]

  private class Impl(collection: Collection) extends HypocentreRepository
