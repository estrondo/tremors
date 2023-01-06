package graboid

import com.softwaremill.macwire.wire
import core.KeyGenerator
import graboid.query.TimeWindowLink
import zio.Task
import zio.ZIO
import zio.stream.Stream
import zio.stream.ZSink

import java.time.ZonedDateTime

trait TimelineManager:

  /** Register record and link with a TimeWindow. */
  def addRecord(record: EventRecord): Task[EventRecord]

  /** Create a TimeWindow and link all unliked EventRecords related to. */
  def createWindow(
      publisherKey: String,
      beginning: ZonedDateTime,
      ending: ZonedDateTime
  ): Task[TimeWindow]

object TimelineManager:

  def apply(
      timeWindowRepository: TimeWindowRepository,
      eventRecordRepository: EventRecordRepository
  ): TimelineManager =
    wire[TimelineManagerImpl]

  private class TimelineManagerImpl(
      timeWindowRepository: TimeWindowRepository,
      eventRecordRepository: EventRecordRepository
  ) extends TimelineManager:

    def timelineDatabase = timeWindowRepository.database

    override def addRecord(record: EventRecord): Task[EventRecord] =
      (for
        _     <- ZIO.logInfo(s"Adding event record") // TODO: Use aspect to carry more information
        added <- eventRecordRepository
                   .add(record)
                   .mapError(reportAddRecordError(record))
                   .tapErrorCause {
                     ZIO.logErrorCause("An error has occurred on adding event record!", _)
                   }
      yield added) @@ EventRecord.Annotation(record)

    override def createWindow(
        publisherKey: String,
        beginning: ZonedDateTime,
        ending: ZonedDateTime
    ): Task[TimeWindow] =
      for
        result <- timeWindowRepository.search(publisherKey, beginning, ending)
        window <- result match
                    case None           => createTimeWindow(publisherKey, beginning, ending)
                    case Some(previous) =>
                      ZIO.fail(
                        GraboidException.IllegalRequest(
                          s"There is already a TimeWindow: ${previous.key}."
                        )
                      )
      yield window

    def createTimeWindow(
        publisherKey: String,
        beginning: ZonedDateTime,
        ending: ZonedDateTime
    ): Task[TimeWindow] =
      val window = TimeWindow(
        key = KeyGenerator.next16(),
        publisherKey = publisherKey,
        beginning = beginning,
        ending = ending,
        successes = 0L,
        failures = 0L
      )

      (for
        added   <- timeWindowRepository.add(window)
        _       <- ZIO.logDebug("A new TimeWindow has been created.")
        updated <- updateUnlikedEventsWith(added)
      yield updated) @@ TimeWindow.Annotation(window)

    def updateUnlikedEventsWith(window: TimeWindow): Task[TimeWindow] =

      def updateIfNecessary(successes: Long) =
        if successes > 0 then
          ZIO.logDebug(s"There have been linked $successes event records.") *> timeWindowRepository
            .update(window.copy(successes = successes))
        else ZIO.succeed(window)

      def linkEvents(stream: Stream[Throwable, EventRecord]) =
        stream
          .map(_.copy(timeWindowKey = Some(window.key)))
          .mapZIO(eventRecordRepository.update)
          .run(ZSink.count)

      for
        stream    <- eventRecordRepository
                       .searchByPublisher(window.publisherKey, Some(TimeWindowLink.Unliked))
        successes <- linkEvents(stream)
        updated   <- updateIfNecessary(successes)
      yield updated

    def reportAddRecordError(record: EventRecord)(cause: Throwable) =
      GraboidException.IllegalState(
        s"It was impossible to add new event record key=${record.key}, publisherKey=${record.publisherKey}!",
        cause
      )
