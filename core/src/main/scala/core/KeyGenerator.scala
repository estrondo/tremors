package core

import java.lang.Integer
import java.lang.Long
import java.security.SecureRandom
import scala.util.Random

trait KeyGenerator:

  def nextShort(): String

  def next(): String

object KeyGenerator extends KeyGenerator:

  private val random = Random(SecureRandom.getInstanceStrong().nextInt())

  override def nextShort(): String =
    fixedLength(Integer.toString(random.nextInt().abs, 32), 6)

  override def next(): String =
    val a       = Long.toString(random.nextLong().abs, 32)
    val b       = Long.toString(random.nextLong().abs, 32)
    val c       = Long.toString(random.nextLong().abs, 32)
    val builder = StringBuilder()
    builder.addAll(fixedLength(a, 10))
    builder.addAll(fixedLength(b, 10))
    builder.addAll(fixedLength(c, 12))
    builder.result()

  private inline def fixedLength(str: String, expectedLength: Int): String =
    if str.length() < expectedLength then fillWithZero(str, expectedLength)
    else if str.length() > expectedLength then trimLength(str, expectedLength)
    else str

  private inline def fillWithZero(str: String, expectedLength: Int): String =
    val builder = StringBuilder()
    for (_ <- str.length() until expectedLength) do builder.addOne('0')
    builder.addAll(str).result()

  private inline def trimLength(str: String, expectedLength: Int): String =
    str.take(expectedLength)
