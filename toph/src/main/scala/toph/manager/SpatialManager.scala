package toph.manager

import com.softwaremill.macwire.wire
import quakeml.{Origin => QOrigin}
import toph.converter.EpicentreConverter
import toph.converter.HypocentreConverter
import toph.model.Epicentre
import toph.model.Hypocentre
import toph.repository.EpicentreRepository
import toph.repository.HypocentreRepository
import zio.Task
import zio.ZIO

trait SpatialManager:

  def accept(origin: QOrigin): Task[(Epicentre, Option[Hypocentre])]

object SpatialManager:

  def apply(epicentreRepository: EpicentreRepository, hypocentreRepository: HypocentreRepository): SpatialManager =
    wire[Impl]

  private class Impl(epicentreRepository: EpicentreRepository, hypocentreRepository: HypocentreRepository)
      extends SpatialManager:

    override def accept(origin: QOrigin): Task[(Epicentre, Option[Hypocentre])] =
      for
        epicentre  <- createEpicentre(origin)
        hypocentre <- createHypocentre(origin)
      yield (epicentre, hypocentre)

    private def createEpicentre(origin: QOrigin): Task[Epicentre] =
      (for
        epicentre <- EpicentreConverter.from(origin)
        _         <- epicentreRepository.add(epicentre)
      yield epicentre)
        .tap(_ => ZIO.logDebug(s"An epicentre for origin=${origin.publicID.uri} was added."))
        .tapErrorCause(
          ZIO.logWarningCause(s"It was impossible to add an epicentre for origin=${origin.publicID.uri}!", _)
        )

    private def createHypocentre(origin: QOrigin): Task[Option[Hypocentre]] =
      (for
        result <- HypocentreConverter.from(origin)
        _      <- result match
                    case Some(hypocentre) => hypocentreRepository.add(hypocentre)
                    case None             => ZIO.unit
      yield result)
        .tap(result =>
          if result.isDefined then ZIO.logDebug(s"An hypocentre for origin=${origin.publicID.uri} was added.")
          else ZIO.logDebug(s"There is no hypocentre for origin=${origin.publicID.uri}.")
        )
        .tapErrorCause(
          ZIO.logWarningCause(s"It was impossible to add a hypocentre for origin=${origin.publicID.uri}!", _)
        )
