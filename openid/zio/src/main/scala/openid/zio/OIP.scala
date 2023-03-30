package openid.zio

import java.net.URL
import javax.crypto.SecretKey
import java.security.PublicKey

final case class OIP(
    key: SecretKey | PublicKey,
    userInfoEndpoint: Option[String]
)
