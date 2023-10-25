package toph.security

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object ClaimsFixture:

  def createRandom(): Claims = Claims(
    id = KeyGenerator.generate(KeyLength.Medium),
    name = s"A person ${KeyGenerator.generate(KeyLength.Short)}",
    email = s"${KeyGenerator.generate(KeyLength.Medium)}@${KeyGenerator.generate(KeyLength.Short)}"
  )
