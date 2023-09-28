package graboid.module

import com.softwaremill.macwire.Module
import graboid.repository.CrawlingExecutionRepository
import graboid.repository.DataCentreRepository
import graboid.time.ZonedDateTimeService
import tremors.zio.farango.DataStore
import tremors.zio.farango.FarangoModule
import zio.Task

@Module
trait RepositoryModule:

  def crawlingExecutionRepository: CrawlingExecutionRepository

  def dataCentreRepository: DataCentreRepository

  def dataStore: DataStore

object RepositoryModule:

  def apply(farangoModule: FarangoModule): Task[RepositoryModule] =
    for
      dataCentreRepository        <- farangoModule
                                       .collection("dataCentre")
                                       .map(DataCentreRepository.apply)
      crawlingExecutionRepository <- farangoModule
                                       .collection("crawlingExecution")
                                       .map(CrawlingExecutionRepository(_, ZonedDateTimeService.Impl))
      dataStoreCollectionManager  <- farangoModule.collection("dataStore")
      dataStore                    = DataStore("graboid", dataStoreCollectionManager)
    yield Impl(crawlingExecutionRepository, dataCentreRepository, dataStore)

  private class Impl(
      val crawlingExecutionRepository: CrawlingExecutionRepository,
      val dataCentreRepository: DataCentreRepository,
      val dataStore: DataStore
  ) extends RepositoryModule
