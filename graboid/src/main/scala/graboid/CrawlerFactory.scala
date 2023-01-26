package graboid

import zio.Task

trait CrawlerFactory:

  def apply(publisher: Publisher, execution: CrawlerExecution): Task[Crawler]
