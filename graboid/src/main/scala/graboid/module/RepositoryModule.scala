package graboid.module

import com.softwaremill.macwire.Module
import com.softwaremill.macwire.wire
import graboid.repository.DataCentreRepository
import tremors.zio.farango.FarangoModule
import zio.Task

@Module
trait RepositoryModule:

  def dataCentreRepository: DataCentreRepository

object RepositoryModule:

  def apply(farangoModule: FarangoModule): Task[RepositoryModule] =
    for dataCentreRepository <- farangoModule
                                  .collection("data-centre")
                                  .map(DataCentreRepository.apply)
    yield wire[Impl]

  private class Impl(
      val dataCentreRepository: DataCentreRepository
  ) extends RepositoryModule
