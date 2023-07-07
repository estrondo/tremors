package graboid.repository

import graboid.FDSNDataCentre
import zio.Task
import zio.stream.ZStream

trait FDSNDataCentreRepository:

  def insert(dataCentre: FDSNDataCentre): Task[FDSNDataCentre]

  def update(dataCentre: FDSNDataCentre): Task[FDSNDataCentre]

  def delete(id: String): Task[FDSNDataCentre]

  def get(id: String): Task[Option[FDSNDataCentre]]

  def all: ZStream[Any, Throwable, FDSNDataCentre]

object FDSNDataCentreRepository
