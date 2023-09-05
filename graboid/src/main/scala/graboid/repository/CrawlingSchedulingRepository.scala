package graboid.repository

import graboid.CrawlingScheduling
import zio.Task
import zio.stream.ZStream

import java.time.ZonedDateTime

trait CrawlingSchedulingRepository:

  def insert(scheduling: CrawlingScheduling): Task[CrawlingScheduling]

  def delete(id: String): Task[CrawlingScheduling]

  def update(scheduling: CrawlingScheduling): Task[CrawlingScheduling]

  def search(moment: ZonedDateTime): ZStream[Any, Throwable, CrawlingScheduling]
