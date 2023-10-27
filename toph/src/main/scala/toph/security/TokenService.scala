package toph.security

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.time.Period
import java.time.ZonedDateTime
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.SecretKey
import toph.ZonedDateTimeService
import zio.Task
import zio.ZIO

trait TokenService:

  def decode(token: String): Task[Option[Claims]]

  def decode(now: ZonedDateTime, token: String): Task[Option[Claims]]

  def encode(claims: Claims): Task[String]

  def encode(expiration: ZonedDateTime, claims: Claims): Task[String]

object TokenService:

  def apply(key: SecretKey, zonedDateTimeService: ZonedDateTimeService, period: Period): TokenService =
    Impl(key, zonedDateTimeService, period)

  private class Impl(key: SecretKey, zonedDateTimeService: ZonedDateTimeService, period: Period) extends TokenService:

    override def decode(token: String): Task[Option[Claims]] =
      for
        now     <- ZIO.succeed(zonedDateTimeService.now())
        decoded <- decode(now, token)
      yield decoded

    override def decode(now: ZonedDateTime, token: String): Task[Option[Claims]] =
      for mac <- createMac()
      yield token.split('.') match
        case Array(expiration, claims, signature) =>
          mac.update(expiration.getBytes())
          mac.update('.'.toByte)
          val expectedSignature = Base64.getEncoder.encodeToString(mac.doFinal(claims.getBytes()))

          if (expectedSignature == signature) {
            decodeBeforeExpire(now, expiration, claims)
          } else {
            None
          }

        case _ =>
          None

    override def encode(claims: Claims): Task[String] =
      for
        expiration <- ZIO.attempt(zonedDateTimeService.now().plus(period))
        token      <- encode(expiration, claims)
      yield token

    override def encode(expiration: ZonedDateTime, claims: Claims): Task[String] =
      for mac <- createMac()
      yield
        val encodedExpiration = writeBase64 { _.writeLong(expiration.toEpochSecond) }
        val encodedClaims     = writeBase64 { dataOutput =>
          dataOutput.writeUTF(claims.id)
          dataOutput.writeUTF(claims.name)
          dataOutput.writeUTF(claims.email)
        }

        val builder = StringBuilder()
          .append(new String(encodedExpiration))
          .append('.')
          .append(new String(encodedClaims))

        mac.update(encodedExpiration)
        mac.update('.'.toByte)

        builder
          .append('.')
          .append(Base64.getEncoder.encodeToString(mac.doFinal(encodedClaims)))
          .result()

    private def createMac(): Task[Mac] =
      ZIO.attempt {
        val mac = Mac.getInstance(key.getAlgorithm)
        mac.init(key)
        mac
      }

    private def decodeBeforeExpire(now: ZonedDateTime, expiration: String, claims: String): Option[Claims] =
      val expirationValue = readBase64(expiration) { _.readLong() }
      if (now.toEpochSecond <= expirationValue) {
        Some(readBase64(claims) { dataInput =>
          val id    = dataInput.readUTF()
          val name  = dataInput.readUTF()
          val email = dataInput.readUTF()
          Claims(id, name, email)
        })
      } else {
        None
      }

    private def readBase64[T](encoded: String)(block: DataInput => T): T =
      block(DataInputStream(ByteArrayInputStream(Base64.getDecoder.decode(encoded))))

    private def writeBase64(block: DataOutput => Unit): Array[Byte] =
      val buffer     = ByteArrayOutputStream()
      val dataOutput = DataOutputStream(buffer)
      block(dataOutput)
      dataOutput.flush()
      Base64.getEncoder.encode(buffer.toByteArray)
