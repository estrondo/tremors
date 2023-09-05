package graboid.crawling

import com.softwaremill.macwire.wire
import graboid.CrawlingScheduling
import graboid.repository.CrawlingSchedulingRepository
import java.time.ZonedDateTime
import zio.Task
import zio.ZIO
import zio.ZIOAspect
import zio.stream.ZStream

trait CrawlingScheduler:

  def add(scheduling: CrawlingScheduling): Task[CrawlingScheduling]

  def remove(id: String): Task[CrawlingScheduling]

  def update(scheduling: CrawlingScheduling): Task[CrawlingScheduling]

  def search(moment: ZonedDateTime): ZStream[Any, Throwable, CrawlingScheduling]

object CrawlingScheduler:

  def apply(repository: CrawlingSchedulingRepository): Task[CrawlingScheduler] =
    ZIO.succeed(wire[Impl])

  private class Impl(repository: CrawlingSchedulingRepository) extends CrawlingScheduler:

    override def add(scheduling: CrawlingScheduling): Task[CrawlingScheduling] =
      (for
        added <- repository
                   .insert(scheduling)
                   .tapErrorCause(ZIO.logErrorCause("It was impossible to add a new scheduling!", _))
        _     <- ZIO.logInfo("New scheduling has been added.")
      yield added) @@ annotateWith(scheduling)

    override def remove(id: String): Task[CrawlingScheduling] =
      for
        removed <- repository
                     .delete(id)
                     .tapErrorCause(ZIO.logErrorCause(s"It was impossible to remove $id!", _))
        _       <- ZIO.logInfo(s"Scheduling $id has benn removed.")
      yield removed

    override def update(scheduling: CrawlingScheduling): Task[CrawlingScheduling] =
      (for
        updated <- repository
                     .update(scheduling)
                     .tapErrorCause(ZIO.logErrorCause(s"It was impossible to update Scheduling.", _))
        _       <- ZIO.logInfo("Scheduling has been updated.")
      yield updated) @@ annotateWith(scheduling)

    private def annotateWith(scheduling: CrawlingScheduling) =
      ZIOAspect.annotated(
        "crawlingScheduling.id"           -> scheduling.id,
        "crawlingScheduling.dataCentreId" -> scheduling.dataCentreId
      )

    override def search(moment: ZonedDateTime): ZStream[Any, Throwable, CrawlingScheduling] =
      ZStream
        .logInfo(s"Searching for scheduling which should run at $moment.")
        .flatMap(_ => repository.search(moment))
