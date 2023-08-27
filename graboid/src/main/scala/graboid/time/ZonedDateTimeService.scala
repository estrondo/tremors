package graboid.time

import java.time.ZonedDateTime
import zio.URIO
import zio.ZIO

trait ZonedDateTimeService:

  def now(): ZonedDateTime

object ZonedDateTimeService:

  def now(): URIO[ZonedDateTimeService, ZonedDateTime] =
    ZIO.serviceWith[ZonedDateTimeService](_.now())
