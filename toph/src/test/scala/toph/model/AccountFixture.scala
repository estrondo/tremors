package toph.model

import scala.util.Random
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength.Medium

object AccountFixture:

  def createRandom(): Account = Account(
    key = KeyGenerator.generate(Medium),
    name = s"Albert-${Random.nextInt(100)}",
    email = s"e-${Random.nextInt(100)}@mc.2",
  )
