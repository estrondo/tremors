package webapi.config

case class ServiceConfig(
    port: Int,
    spatial: SpatialServiceConfig
)

case class SpatialServiceConfig(
    toph: String
)
