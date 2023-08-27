package graboid.time

import java.time.ZonedDateTime
import zio.URIO
import zio.ZIO

trait Time:

  def now(): ZonedDateTime

object Time:

  def now(): URIO[Time, ZonedDateTime] =
    ZIO.serviceWith[Time](_.now())
