package graboid.config

import farango.zio.starter.ArangoConfig
import graboid.fdsn.FDSNCrawler
import zkafka.starter.KafkaConfig

case class GraboidConfig(
    arango: ArangoConfig,
    httpClient: HttpClientConfig,
    kafka: KafkaConfig,
    crawlerExecutor: CrawlerExecutorConfig
)
