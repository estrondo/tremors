package graboid.config

import graboid.fdsn.FDSNCrawler

import farango.zio.starter.ArangoConfig
case class GraboidConfig(
    arango: ArangoConfig,
    httpClient: HttpClientConfig,
    kafka: KafkaConfig,
    crawlerExecutor: CrawlerExecutorConfig
)
