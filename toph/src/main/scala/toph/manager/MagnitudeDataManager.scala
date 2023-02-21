package toph.manager

import com.softwaremill.macwire.wire
import quakeml.QuakeMLMagnitude
import toph.converter.MagnitudeDataConverter
import toph.model.data.MagnitudeData
import toph.repository.MagnitudeDataRepository
import zio.Task
import zio.ZIO

trait MagnitudeDataManager:

  def accept(magnitude: QuakeMLMagnitude): Task[MagnitudeData]

object MagnitudeDataManager:

  def apply(repository: MagnitudeDataRepository): MagnitudeDataManager =
    wire[Impl]

  private class Impl(repository: MagnitudeDataRepository) extends MagnitudeDataManager:

    override def accept(magnitude: QuakeMLMagnitude): Task[MagnitudeData] =
      (for
        converted <- MagnitudeDataConverter.fromQMagnitude(magnitude)
        result    <- repository.add(converted)
      yield result)
        .tap(_ => ZIO.logDebug(s"A magnitude with key=${magnitude.publicID.uri} was added."))
        .tapErrorCause(
          ZIO.logWarningCause(s"It was impossible to add magnitude with key=${magnitude.publicID.uri}!", _)
        )
