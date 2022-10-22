package tremors.graboid.config

import tremors.graboid.CrawlerManager

case class CrawlerManagerConfig(
    concurrency: Int
):

  def materialized = CrawlerManager.Config(Option(concurrency))
