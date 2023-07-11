package tremors.generator

import java.util.concurrent.ThreadLocalRandom

sealed abstract class KeyLength(val length: Int)

object KeyLength:

  case object Short extends KeyLength(2)

  case object Medium extends KeyLength(4)

  case object Long extends KeyLength(8)

trait KeyGenerator:

  def generate(length: KeyLength): String

object KeyGenerator extends KeyGenerator:

  private val E15 = 1 << 15
  private val E10 = 1 << 10
  private val E5  = 1 << 5

  override def generate(length: KeyLength): String =
    val random  = ThreadLocalRandom.current()
    val builder = StringBuilder()
    for _ <- 0 until length.length do builder.addAll(generate(random))
    builder.result()

  private def generate(random: ThreadLocalRandom): String =
    val value = random.nextInt() & 0xfffff
    val str   = Integer.toString(value, 32)

    if value > E15 then str
    else if value > E10 then s"0$str"
    else if value > E5 then s"00$str"
    else s"000$str"
