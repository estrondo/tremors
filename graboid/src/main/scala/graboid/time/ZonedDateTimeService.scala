package graboid.time

import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import zio.ULayer
import zio.URIO
import zio.ZIO
import zio.ZLayer

trait ZonedDateTimeService:

  def now(): ZonedDateTime

object ZonedDateTimeService:

  val live: ULayer[ZonedDateTimeService] = ZLayer.succeed(Impl)

  def now(): URIO[ZonedDateTimeService, ZonedDateTime] =
    ZIO.serviceWith[ZonedDateTimeService](_.now())

  object Impl extends ZonedDateTimeService:

    private val zoneId = Clock.systemUTC().getZone

    override def now(): ZonedDateTime = ZonedDateTime.now(zoneId).truncatedTo(ChronoUnit.SECONDS)
