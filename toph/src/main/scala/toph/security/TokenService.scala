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
import toph.TimeService
import toph.model.Account
import zio.Task
import zio.ZIO

trait TokenService:

  def decode(token: String): Task[Option[Token]]

  def encode(account: Account): Task[Token]

object TokenService:

  def apply(key: SecretKey, timeService: TimeService, period: Period, b64: B64): TokenService =
    Impl(key, timeService, period, b64)

  private class Impl(key: SecretKey, timeService: TimeService, period: Period, b64: B64) extends TokenService:

    override def decode(token: String): Task[Option[Token]] =
      decodeValidSignature(timeService.zonedDateTimeNow(), token)

    private def decodeValidSignature(now: ZonedDateTime, token: String): Task[Option[Token]] =
      for mac <- createMac()
      yield token.split('.') match
        case Array(expiration, account, signature) =>
          mac.update(expiration.getBytes())
          val expectedSignature = b64.encodeToString(mac.doFinal(account.getBytes()))

          if (expectedSignature == signature) {
            for account <- decodeBeforeExpire(now, expiration, account)
            yield Token(account, token)
          } else {
            None
          }

        case _ =>
          None

    override def encode(account: Account): Task[Token] =
      encode(timeService.zonedDateTimeNow().plus(period), account)

    private def encode(expiration: ZonedDateTime, account: Account): Task[Token] =
      for mac <- createMac()
      yield
        val encodedExpiration = writeBase64 { _.writeLong(expiration.toEpochSecond) }
        val encodedAccount    = writeBase64 { dataOutput =>
          dataOutput.writeUTF(account.key)
          dataOutput.writeUTF(account.name)
          dataOutput.writeUTF(account.email)
        }

        val builder = StringBuilder()
          .append(new String(encodedExpiration))
          .append('.')
          .append(new String(encodedAccount))

        mac.update(encodedExpiration)

        val generated = builder
          .append('.')
          .append(b64.encodeToString(mac.doFinal(encodedAccount)))
          .result()

        Token(account, generated)

    private def createMac(): Task[Mac] =
      ZIO.attempt {
        val mac = Mac.getInstance(key.getAlgorithm)
        mac.init(key)
        mac
      }

    private def decodeBeforeExpire(now: ZonedDateTime, expiration: String, claims: String): Option[Account] =
      val expirationValue = readBase64(expiration) { _.readLong() }
      if now.toEpochSecond <= expirationValue then
        Some(readBase64(claims) { dataInput =>
          val id    = dataInput.readUTF()
          val name  = dataInput.readUTF()
          val email = dataInput.readUTF()
          Account(key = id, name = name, email = email)
        })
      else None

    private def readBase64[T](encoded: String)(block: DataInput => T): T =
      block(DataInputStream(ByteArrayInputStream(b64.decode(encoded))))

    private def writeBase64(block: DataOutput => Unit): Array[Byte] =
      val buffer     = ByteArrayOutputStream()
      val dataOutput = DataOutputStream(buffer)
      block(dataOutput)
      dataOutput.flush()
      b64.encodeToBytes(buffer.toByteArray)
