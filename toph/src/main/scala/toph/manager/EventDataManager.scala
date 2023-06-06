package toph.manager

import com.softwaremill.macwire.wire
import quakeml.QuakeMLDetectedEvent
import toph.converter.EventDataConverter
import toph.model.data.EventData
import toph.model.data.HypocentreData
import toph.model.data.MagnitudeData
import toph.repository.EventDataRepository
import zio.Task
import zio.ZIO

trait EventDataManager:

  def accept(event: QuakeMLDetectedEvent): Task[(EventData, Seq[HypocentreData], Seq[MagnitudeData])]

object EventDataManager:

  def apply(
      repository: EventDataRepository,
      spatialManager: SpatialManager,
      magnitudeManager: MagnitudeDataManager
  ): EventDataManager =
    wire[Impl]

  private class Impl(
      repository: EventDataRepository,
      spatialManager: SpatialManager,
      magnitudeManager: MagnitudeDataManager
  ) extends EventDataManager:
    override def accept(detected: QuakeMLDetectedEvent): Task[(EventData, Seq[HypocentreData], Seq[MagnitudeData])] =
      (for
        event       <- EventDataConverter.fromQEvent(detected.event)
        _           <- add(event)
        magnitudes  <- ZIO.foreach(detected.event.magnitude)(magnitudeManager.accept)
        hypocentres <- ZIO.foreach(detected.event.origin)(spatialManager.accept)
        _           <- spatialManager.createEvents(event, hypocentres, magnitudes)
      yield (event, hypocentres, magnitudes))
        .tap(_ => ZIO.logInfo(s"A detected with key=${detected.event.publicID.uri} event was accepted."))
        .tapErrorCause(
          ZIO.logWarningCause(
            s"It was impossible to accept a detected event with key=${detected.event.publicID.uri}!",
            _
          )
        )

    private def add(event: EventData): Task[EventData] =
      repository
        .add(event)
        .tap(_ => ZIO.logDebug(s"A new event with key=${event.key} was added."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add event with key=${event.key}!", _))
