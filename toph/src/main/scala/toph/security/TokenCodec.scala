package toph.security

import com.softwaremill.macwire.wire
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutputStream
import java.time.ZonedDateTime
import java.util.random.RandomGenerator
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

  def apply(keys: IndexedSeq[SecretKey], randomGenerator: RandomGenerator): TokenCodec =
    wire[Impl]

  private def validateAndExtractVersion(input: DataInput): Try[Int] =
    val version = input.readUnsignedByte()
    if version == Version then Success(version) else Failure(TophException.Security(s"Invalid signature: $version!"))

  private def validateAndExtractExpiration(input: DataInput, now: ZonedDateTime): Try[Long] =
    val expiration = input.readLong()
    if expiration < now.toEpochSecond then Success(expiration) else Failure(TophException.Security("Expired token!"))

  class Impl(keys: IndexedSeq[SecretKey], randomGenerator: RandomGenerator) extends TokenCodec:
    assume(keys.nonEmpty && keys.length < 8, "You should configure from 1 to 8 secret keys!")

    override def decode(token: Array[Byte], now: ZonedDateTime): Task[Account] =
      ZIO.fromTry {
        for {
          input              <- Try(DataInputStream(ByteArrayInputStream(token)))
          version            <- validateAndExtractVersion(input)
          expiration         <- validateAndExtractExpiration(input, now)
          (_, payloadLength) <- validateSignature(input, token)
          account            <- extractAccount(DataInputStream(ByteArrayInputStream(token, 15, payloadLength - 15)))
        } yield account
      }

    private def validateSignature(input: DataInput, token: Array[Byte]): Try[(Array[Byte], Int)] =
      val payloadLength = input.readUnsignedShort()
      val mac           = createMacFor(input.readInt())
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
      ZIO.fromTry {
        for (mac, keyIndex) <- Try(createMac())
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

          var info = 0x00
          info |= (0x07 & keyIndex)

          headerOutput.writeInt(info)

          val headerBuffer = header.toByteArray

          mac.update(headerBuffer)
          val signature = mac.doFinal(bodyBuffer)

          Array
            .newBuilder[Byte]
            .addAll(headerBuffer)
            .addAll(bodyBuffer)
            .addAll(signature)
            .result()
      }

    private def createMac(): (Mac, Int) =
      val index = randomGenerator.nextInt(keys.length)
      val key   = keys(index)
      val mac   = Mac.getInstance(key.getAlgorithm)
      mac.init(key)
      (mac, index)

    private def createMacFor(info: Int): Mac =
      val key = keys(info & 0x07)
      val mac = Mac.getInstance(key.getAlgorithm)
      mac.init(key)
      mac
