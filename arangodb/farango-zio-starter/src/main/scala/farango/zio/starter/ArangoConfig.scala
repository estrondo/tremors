package farango.zio.starter

import zio.config.magnolia.Descriptor

case class ArangoConfig(
    database: String,
    username: String,
    password: String,
    hosts: Seq[ArangoHost]
)

object ArangoHost:

  def toArangoHost(value: String): Seq[ArangoHost] =
    for part <- value.split("\\s*,\\s*")
    yield part.split(":") match
      case Array(hostname, port) => ArangoHost(hostname, port.toInt)
      case _                     => throw IllegalArgumentException(value)

  def fromArangoHost(values: Seq[ArangoHost]): String =
    throw IllegalStateException("fromArangoHost")

  given seqArangoHost: Descriptor[Seq[ArangoHost]] =
    Descriptor.from(Descriptor[String].transform(toArangoHost, fromArangoHost))

case class ArangoHost(hostname: String, port: Int)
