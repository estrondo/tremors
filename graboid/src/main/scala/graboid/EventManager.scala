package graboid

import zio.Task

trait EventManager:

  def register(info: Crawler.Info, publisher: Publisher, execution: CrawlerExecution): Task[Crawler.Info]
