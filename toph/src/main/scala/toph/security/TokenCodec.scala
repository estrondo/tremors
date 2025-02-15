package toph.security

import com.softwaremill.macwire.wire
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutputStream
import java.time.ZonedDateTime
import javax.crypto.Mac
import javax.crypto.SecretKey
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import toph.TophException
import toph.model.Account
import zio.Task
import zio.ZIO

trait TokenCodec:

  def decode(token: Array[Byte], now: ZonedDateTime): Task[Account]

  def encode(account: Account, expiration: ZonedDateTime): Task[Array[Byte]]

object TokenCodec:

  val Version = 1

  def apply(key: SecretKey): TokenCodec =
    wire[Impl]

  private def validateAndExtractVersion(input: DataInput): Try[Int] =
    val version = input.readUnsignedByte()
    if version == Version then Success(version) else Failure(TophException.Security(s"Invalid signature: $version!"))

  private def validateAndExtractExpiration(input: DataInput, now: ZonedDateTime): Try[Long] =
    val expiration = input.readLong()
    if expiration < now.toEpochSecond then Success(expiration) else Failure(TophException.Security("Expired token!"))

  class Impl(key: SecretKey) extends TokenCodec:

    override def decode(token: Array[Byte], now: ZonedDateTime): Task[Account] =
      createMac().flatMap { mac =>
        val input = DataInputStream(ByteArrayInputStream(token))
        ZIO.fromTry {
          for {
            version            <- validateAndExtractVersion(input)
            expiration         <- validateAndExtractExpiration(input, now)
            (_, payloadLength) <- validateSignature(input, token, mac)
            account            <- extractAccount(DataInputStream(ByteArrayInputStream(token, 15, payloadLength - 15)))
          } yield account
        }
      }

    private def validateSignature(input: DataInput, token: Array[Byte], mac: Mac): Try[(Array[Byte], Int)] =
      val payloadLength = input.readUnsignedShort()
      mac.update(token, 0, payloadLength)

      val s = mac.doFinal()
      if java.util.Arrays.equals(s, 0, s.length, token, payloadLength, token.length) then Success((s, payloadLength))
      else Failure(TophException.Security("Invalid signature!"))

    private def extractAccount(input: DataInput): Try[Account] =
      Try {
        val key   = input.readUTF()
        val email = input.readUTF()
        val name  = input.readUTF()
        Account(key, email, name)
      }

    /** What is the token?
      *
      * Token: Envelope + Signature
      *
      * Envelope = Header + Body
      *
      * @param account
      * @param expiration
      * @return
      */
    def encode(account: Account, expiration: ZonedDateTime): Task[Array[Byte]] =
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

        Array
          .newBuilder[Byte]
          .addAll(headerBuffer)
          .addAll(bodyBuffer)
          .addAll(signature)
          .result()

    private def createMac(): Task[Mac] =
      ZIO.attempt {
        val mac = Mac.getInstance(key.getAlgorithm)
        mac.init(key)
        mac
      }
