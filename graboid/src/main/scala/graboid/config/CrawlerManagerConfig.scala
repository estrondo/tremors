package graboid.config

import graboid.CrawlerManager

case class CrawlerManagerConfig(
    concurrency: Int
):

  def materialized = CrawlerManager.Config(Option(concurrency))
