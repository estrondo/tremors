package core

import java.lang.Integer
import java.lang.Long
import java.security.SecureRandom
import scala.util.Random
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

trait KeyGenerator:

  def next4(): String

  def next8(): String

  def next16(): String

  def next32(): String

object KeyGenerator extends KeyGenerator:

  private def random = ThreadLocalRandom.current()

  private val Radix = 32

  private val E3 = (32 * 32 * 32) - 1

  private val E2 = (32 * 32) - 1

  private val E1 = 31

  override def next4(): String = generate(1)

  override def next8(): String = generate(2)

  override def next16(): String = generate(4)

  override def next32(): String = generate(8)

  private def generate(count: Int): String =
    val builder = StringBuilder()

    for _ <- 0 until count do builder.addAll(nextFragment())

    builder.result()

  private inline def nextFragment(): String =
    val value = random.nextInt() & 0xfffff
    val str   = Integer.toString(value, Radix)

    if value > E3 then str
    else if value > E2 then "0" + str
    else if value > E1 then "00" + str
    else "000" + str
