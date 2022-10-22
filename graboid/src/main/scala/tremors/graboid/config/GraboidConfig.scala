package tremors.graboid.config

import tremors.graboid.fdsn.FDSNCrawler

case class GraboidConfig(
    crawlerRepository: ArangoConfig,
    timelineRepository: ArangoConfig,
    crawlerManager: CrawlerManagerConfig,
    httpClient: HttpClientConfig
)
