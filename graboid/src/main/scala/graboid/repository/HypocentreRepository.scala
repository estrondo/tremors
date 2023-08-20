package graboid.repository

import com.softwaremill.macwire.wire
import tremors.zio.farango.CollectionManager

trait HypocentreRepository

object HypocentreRepository:

  def apply(collectionManager: CollectionManager): HypocentreRepository =
    wire[Impl]

  private class Impl(collectionManager: CollectionManager) extends HypocentreRepository
