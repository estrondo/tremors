package toph.model

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object TophUserFixture:

  def createRandom(): TophUser = TophUser(
    id = KeyGenerator.generate(KeyLength.Medium),
    name = KeyGenerator.generate(KeyLength.Long),
    email = s"${KeyGenerator.generate(KeyLength.Medium)}@${KeyGenerator.generate(KeyLength.Short)}"
  )
