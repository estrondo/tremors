package toph.config

case class SecurityConfig(
    secret: String,
    algorithm: String,
    tokenExpiration: Int
)
