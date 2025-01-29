package toph.security

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutputStream
import java.time.Period
import java.time.ZonedDateTime
import javax.crypto.Mac
import javax.crypto.SecretKey
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import toph.TimeService
import toph.TophException
import toph.model.Account
import zio.Cause
import zio.Task
import zio.ZIO

trait TokenService:

  def decode(token: Array[Byte]): Task[Option[Token]]

  def encode(account: Account): Task[Token]

object TokenService:

  val Version = 1

  def apply(key: SecretKey, timeService: TimeService, period: Period): TokenService =
    Impl(key, timeService, period)

  private class Impl(key: SecretKey, timeService: TimeService, period: Period) extends TokenService:

    override def decode(token: Array[Byte]): Task[Option[Token]] =
      decode(timeService.zonedDateTimeNow(), token)
        .catchAll { cause =>
          ZIO.logErrorCause("Unable to validate token!", Cause.die(cause)) *> ZIO.none
        }

    private def decode(now: ZonedDateTime, token: Array[Byte]): Task[Option[Token]] =
      createMac().flatMap { mac =>
        val input = DataInputStream(ByteArrayInputStream(token))
        ZIO.fromTry {
          for {
            version             <- validateVersion(input)
            expiration          <- validateExpiration(input, now)
            (signature, offset) <- validateSignature(input, token, mac)
            account             <- extractAccount(DataInputStream(ByteArrayInputStream(token, 15, offset - 15)))
          } yield Some(Token(account, token))
        }
      }

    private def validateVersion(input: DataInput): Try[Int] =
      val version = input.readUnsignedByte()
      if version == Version then Success(version) else Failure(TophException.Security(s"Invalid signature: $version!"))

    private def validateExpiration(input: DataInput, now: ZonedDateTime): Try[Long] =
      val expiration = input.readLong()
      if expiration < now.toEpochSecond then Success(expiration) else Failure(TophException.Security("Expired token!"))

    private def validateSignature(input: DataInput, token: Array[Byte], mac: Mac): Try[(Array[Byte], Int)] =
      val offset = input.readUnsignedShort()
      mac.update(token, 0, offset)

      val s = mac.doFinal()
      if java.util.Arrays.equals(s, 0, s.length, token, offset, token.length) then Success((s, offset))
      else Failure(TophException.Security("Invalid signature!"))

    private def extractAccount(input: DataInput): Try[Account] =
      Try {
        val key   = input.readUTF()
        val email = input.readUTF()
        val name  = input.readUTF()
        Account(key, email, name)
      }

    override def encode(account: Account): Task[Token] =
      encode(timeService.zonedDateTimeNow().plus(period), account)

    private def encode(expiration: ZonedDateTime, account: Account): Task[Token] =
      for mac <- createMac()
      yield
        val body       = ByteArrayOutputStream(512)
        val bodyOutput = new DataOutputStream(body)
        bodyOutput.writeUTF(account.key)
        bodyOutput.writeUTF(account.email)
        bodyOutput.writeUTF(account.name)
        bodyOutput.writeShort(0x0) // additional length
        val bodyBuffer = body.toByteArray

        val header       = ByteArrayOutputStream()
        val headerOutput = DataOutputStream(header)
        headerOutput.writeByte(Version)
        headerOutput.writeLong(expiration.toEpochSecond)
        headerOutput.writeShort(15 + bodyBuffer.length)
        headerOutput.writeInt(0x0) // reserved (maybe flags)
        val headerBuffer = header.toByteArray

        mac.update(headerBuffer)
        val signature = mac.doFinal(bodyBuffer)

        val token = Array
          .newBuilder[Byte]
          .addAll(headerBuffer)
          .addAll(bodyBuffer)
          .addAll(signature)
          .result()

        Token(account, token)

    private def createMac(): Task[Mac] =
      ZIO.attempt {
        val mac = Mac.getInstance(key.getAlgorithm)
        mac.init(key)
        mac
      }
