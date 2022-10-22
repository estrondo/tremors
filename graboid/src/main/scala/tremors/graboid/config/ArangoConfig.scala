package tremors.graboid.config

case class ArangoConfig(
    database: String,
    username: String,
    password: String,
    hosts: Seq[ArangoHost]
)

case class ArangoHost(hostname: String, port: Int)
