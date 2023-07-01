package tremors.zio.farango

case class ArangoConfig(
    hosts: String,
    username: String,
    password: String,
    database: String,
    rootPassword: String
)
