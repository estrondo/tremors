package toph.model

import toph.security.Claims
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object ClaimsFixture:

  def createRandom(): Claims = Claims(
    id = KeyGenerator.generate(KeyLength.Long),
    name = s"Name ${KeyGenerator.generate(KeyLength.Short)}",
    email = s"${KeyGenerator.generate(KeyLength.Medium)}@${KeyGenerator.generate(KeyLength.Short)}"
  )
