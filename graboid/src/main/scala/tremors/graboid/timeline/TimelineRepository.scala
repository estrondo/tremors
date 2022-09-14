package tremors.graboid.repository

import zio.*
import tremors.graboid.Crawler

object TimelineRepository

trait TimelineRepository:

  def nextInterval(name: String, intervalLong: Int): UIO[Option[Crawler.Interval]]
