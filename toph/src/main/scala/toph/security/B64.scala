package toph.security

import java.nio.charset.StandardCharsets
import java.util.Base64

trait B64:

  def decode(bytes: Array[Byte]): Array[Byte]

  def decode(bytes: String): Array[Byte] =
    decode(bytes.getBytes(StandardCharsets.UTF_8))

  def encodeToBytes(bytes: Array[Byte]): Array[Byte]

  def encodeToString(bytes: Array[Byte]): String

object B64 extends B64:

  def decode(bytes: Array[Byte]): Array[Byte] =
    Base64.getDecoder.decode(bytes)

  def encodeToString(bytes: Array[Byte]): String =
    Base64.getEncoder.encodeToString(bytes)

  def encodeToBytes(bytes: Array[Byte]): Array[Byte] =
    Base64.getEncoder.encode(bytes)
