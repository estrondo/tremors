package graboid.manager

import com.softwaremill.macwire.wire
import graboid.DataCentre
import graboid.logDebug
import graboid.logErrorCause
import graboid.logInfo
import graboid.repository.DataCentreRepository
import zio.Task
import zio.stream.ZStream

trait DataCentreManager:

  def add(dataCentre: DataCentre): Task[DataCentre]

  def update(dataCentre: DataCentre): Task[DataCentre]

  def delete(id: String): Task[DataCentre]

  def get(id: String): Task[Option[DataCentre]]

  def all: ZStream[Any, Throwable, DataCentre]

object DataCentreManager:

  def apply(repository: DataCentreRepository): DataCentreManager =
    wire[Impl]

  private class Impl(repository: DataCentreRepository) extends DataCentreManager:
    override def add(dataCentre: DataCentre): Task[DataCentre] =
      repository
        .insert(dataCentre)
        .tap(logInfo(s"The Data Centre ${dataCentre.id} was added."))
        .tapErrorCause(logErrorCause(s"It was impossible to add data centre: ${dataCentre.id}!"))

    override def update(dataCentre: DataCentre): Task[DataCentre] =
      repository
        .update(dataCentre)
        .tap(logDebug(s"The Data Centre ${dataCentre.id} was updated."))
        .tapErrorCause(logErrorCause(s"It was impossible to update the data centre ${dataCentre.id}!"))

    override def delete(id: String): Task[DataCentre] =
      repository
        .delete(id)
        .tap(logDebug(s"The Data Centre $id was removed."))
        .tapErrorCause(logErrorCause(s"It was impossible to delete the data centre $id!"))

    override def get(id: String): Task[Option[DataCentre]] =
      repository
        .get(id)
        .tapErrorCause(logErrorCause(s"It was impossible to get the data centre $id!"))

    override def all: ZStream[Any, Throwable, DataCentre] =
      repository.all
        .tapErrorCause(logErrorCause("It was impossible to get the list of data centres!"))
