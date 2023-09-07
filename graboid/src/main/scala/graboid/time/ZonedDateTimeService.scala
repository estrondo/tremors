package graboid.time

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import zio.URIO
import zio.ZEnvironment
import zio.ZIO

trait ZonedDateTimeService:

  def now(): ZonedDateTime

object ZonedDateTimeService:

  val live: ZEnvironment[ZonedDateTimeService] = ZEnvironment(new Impl)

  def now(): URIO[ZonedDateTimeService, ZonedDateTime] =
    ZIO.serviceWith[ZonedDateTimeService](_.now())

  private class Impl extends ZonedDateTimeService:
    override def now(): ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)
