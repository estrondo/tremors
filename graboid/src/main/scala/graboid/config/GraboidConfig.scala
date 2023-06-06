package graboid.config

import farango.zio.starter.ArangoConfig
import zkafka.starter.KafkaConfig

case class GraboidConfig(
    arango: ArangoConfig,
    httpClient: HttpClientConfig,
    kafka: KafkaConfig,
    crawlerExecutor: CrawlerExecutorConfig
)
