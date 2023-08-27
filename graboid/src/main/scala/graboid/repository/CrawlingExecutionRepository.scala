package graboid.repository

import graboid.CrawlingExecution
import zio.Task

trait CrawlingExecutionRepository:

  def insert(execution: CrawlingExecution): Task[CrawlingExecution]

  def updateCounting(execution: CrawlingExecution): Task[CrawlingExecution]

  def updateState(execution: CrawlingExecution): Task[CrawlingExecution]
