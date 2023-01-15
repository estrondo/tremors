package graboid.config

import graboid.fdsn.FDSNCrawler

case class GraboidConfig(
    httpClient: HttpClientConfig,
    kafka: KafkaConfig
)
