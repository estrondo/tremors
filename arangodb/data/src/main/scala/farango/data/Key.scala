package farango.data

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Key:

  def safe(x: String): String = URLEncoder.encode(x, StandardCharsets.UTF_8)

  def unsafe(x: String): String = URLDecoder.decode(x, StandardCharsets.UTF_8)

case class Key(value: String)
