package graboid.manager

import graboid.DataCentre
import graboid.repository.DataCentreRepository
import zio.Task
import zio.ZIO
import zio.stream.ZStream

trait DataCentreManager:

  def add(dataCentre: DataCentre): Task[DataCentre]

  def update(dataCentre: DataCentre): Task[DataCentre]

  def delete(id: String): Task[DataCentre]

  def get(id: String): Task[Option[DataCentre]]

  def all: ZStream[Any, Throwable, DataCentre]

object DataCentreManager:

  def apply(repository: DataCentreRepository): DataCentreManager =
    Impl(repository)

  private class Impl(repository: DataCentreRepository) extends DataCentreManager:
    override def add(dataCentre: DataCentre): Task[DataCentre] =
      repository
        .insert(dataCentre)
        .tap(_ => ZIO.logInfo(s"The Data Centre ${dataCentre.id} was added."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to add data centre: ${dataCentre.id}!", _))

    override def update(dataCentre: DataCentre): Task[DataCentre] =
      repository
        .update(dataCentre)
        .tap(_ => ZIO.logDebug(s"The Data Centre ${dataCentre.id} was updated."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to update the data centre ${dataCentre.id}!", _))

    override def delete(id: String): Task[DataCentre] =
      repository
        .delete(id)
        .tap(_ => ZIO.logDebug(s"The Data Centre $id was removed."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to delete the data centre $id!", _))

    override def get(id: String): Task[Option[DataCentre]] =
      repository
        .get(id)
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible to get the data centre $id!", _))

    override def all: ZStream[Any, Throwable, DataCentre] =
      repository.all
        .tapErrorCause(ZIO.logErrorCause("It was impossible to get the list of data centres!", _))
