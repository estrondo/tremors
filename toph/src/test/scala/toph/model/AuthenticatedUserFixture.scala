package toph.model

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object AuthenticatedUserFixture:

  def createRandom() =
    val expiration = KeyGenerator.generate(KeyLength.Short)
    val claims     = KeyGenerator.generate(KeyLength.Medium)
    val signature  = KeyGenerator.generate(KeyLength.Long)

    AuthenticatedUser(
      token = s"$expiration.$claims.$signature",
      claims = ClaimsFixture.createRandom()
    )
