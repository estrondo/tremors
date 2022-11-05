package graboid.config

import graboid.fdsn.FDSNCrawler

case class GraboidConfig(
    crawlerRepository: ArangoConfig,
    timelineRepository: ArangoConfig,
    crawlerManager: CrawlerManagerConfig,
    httpClient: HttpClientConfig
)
