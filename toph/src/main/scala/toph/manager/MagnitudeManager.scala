package toph.manager

import com.softwaremill.macwire.wire
import quakeml.{Magnitude => QMagnitude}
import toph.converter.MagnitudeConverter
import toph.model.Magnitude
import toph.repository.MagnitudeRepository
import zio.Task
import zio.ZIO

trait MagnitudeManager:

  def accept(magnitude: QMagnitude): Task[Magnitude]

object MagnitudeManager:

  def apply(repository: MagnitudeRepository): MagnitudeManager =
    wire[Impl]

  private class Impl(repository: MagnitudeRepository) extends MagnitudeManager:

    override def accept(magnitude: QMagnitude): Task[Magnitude] =
      (for
        converted <- MagnitudeConverter.fromQMagnitude(magnitude)
        result    <- repository.add(converted)
      yield result)
        .tap(_ => ZIO.logDebug(s"A magnitude with key=${magnitude.publicID.uri} was added."))
        .tapErrorCause(
          ZIO.logWarningCause(s"It was impossible to add magnitude with key=${magnitude.publicID.uri}!", _)
        )
