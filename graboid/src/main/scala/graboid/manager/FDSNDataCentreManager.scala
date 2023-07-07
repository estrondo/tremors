package graboid.manager

import com.softwaremill.macwire.wire
import graboid.FDSNDataCentre
import graboid.logDebug
import graboid.logErrorCause
import graboid.logInfo
import graboid.repository.FDSNDataCentreRepository
import zio.Task
import zio.stream.ZStream

trait FDSNDataCentreManager:

  def add(dataCentre: FDSNDataCentre): Task[FDSNDataCentre]

  def update(dataCentre: FDSNDataCentre): Task[FDSNDataCentre]

  def delete(id: String): Task[FDSNDataCentre]

  def get(id: String): Task[Option[FDSNDataCentre]]

  def all: ZStream[Any, Throwable, FDSNDataCentre]

object FDSNDataCentreManager:

  def apply(repository: FDSNDataCentreRepository): FDSNDataCentreManager =
    wire[Impl]

  private class Impl(repository: FDSNDataCentreRepository) extends FDSNDataCentreManager:
    override def add(dataCentre: FDSNDataCentre): Task[FDSNDataCentre] =
      repository
        .insert(dataCentre)
        .tap(logInfo(s"The Data Centre ${dataCentre.id} was added."))
        .tapErrorCause(logErrorCause(s"It was impossible to add data centre: ${dataCentre.id}!"))

    override def update(dataCentre: FDSNDataCentre): Task[FDSNDataCentre] =
      repository
        .update(dataCentre)
        .tap(logDebug(s"The Data Centre ${dataCentre.id} was updated."))
        .tapErrorCause(logErrorCause(s"It was impossible to update the data centre ${dataCentre.id}!"))

    override def delete(id: String): Task[FDSNDataCentre] =
      repository
        .delete(id)
        .tap(logDebug(s"The Data Centre $id was removed."))
        .tapErrorCause(logErrorCause(s"It was impossible to delete the data centre $id!"))

    override def get(id: String): Task[Option[FDSNDataCentre]] =
      repository
        .get(id)
        .tapErrorCause(logErrorCause(s"It was impossible to get the data centre $id!"))

    override def all: ZStream[Any, Throwable, FDSNDataCentre] =
      repository.all
        .tapErrorCause(logErrorCause("It was impossible to get the list of data centres!"))
