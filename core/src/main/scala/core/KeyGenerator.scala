package core

import core.KeyGenerator.KeyLength
import java.lang.Integer
import java.util.concurrent.ThreadLocalRandom
import scala.util.Random

trait KeyGenerator:

  def next(length: KeyLength): String

  def next4(): String = next(KeyLength.L4)

  def next8(): String = next(KeyLength.L8)

  def next16(): String = next(KeyLength.L16)

  def next32(): String = next(KeyLength.L32)

  def next64(): String = next(KeyLength.L64)

object KeyGenerator extends KeyGenerator:

  enum KeyLength(val value: Int):
    case L4  extends KeyLength(1)
    case L8  extends KeyLength(2)
    case L12 extends KeyLength(3)
    case L16 extends KeyLength(4)
    case L20 extends KeyLength(5)
    case L24 extends KeyLength(6)
    case L28 extends KeyLength(7)
    case L32 extends KeyLength(8)
    case L36 extends KeyLength(9)
    case L40 extends KeyLength(10)
    case L44 extends KeyLength(11)
    case L48 extends KeyLength(12)
    case L52 extends KeyLength(13)
    case L56 extends KeyLength(14)
    case L60 extends KeyLength(15)
    case L64 extends KeyLength(16)

  private val Radix = 32

  private val E3 = (32 * 32 * 32) - 1

  private val E2 = (32 * 32) - 1

  private val E1 = 31

  override def next(length: KeyLength): String = generate(length.value)

  private def generate(count: Int): String =
    val random  = ThreadLocalRandom.current()
    val builder = StringBuilder()

    for _ <- 0 until count do builder.addAll(nextFragment(random))

    builder.result()

  private inline def nextFragment(inline random: Random): String =
    val value = random.nextInt() & 0xfffff
    val str   = Integer.toString(value, Radix)

    if value > E3 then str
    else if value > E2 then "0" + str
    else if value > E1 then "00" + str
    else "000" + str
