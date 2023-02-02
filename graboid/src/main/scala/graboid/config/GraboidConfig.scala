package graboid.config

import graboid.fdsn.FDSNCrawler

case class GraboidConfig(
    arango: ArangoConfig,
    httpClient: HttpClientConfig,
    kafka: KafkaConfig,
    crawlerExecutor: CrawlerExecutorConfig
)
