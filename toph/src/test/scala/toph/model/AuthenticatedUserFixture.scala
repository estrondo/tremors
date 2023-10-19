package toph.model

import java.time.Clock
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object AuthenticatedUserFixture:

  def createRandom() =
    AuthenticatedUser(
      token = KeyGenerator.generate(KeyLength.Long),
      expiration = ZonedDateTime.now(Clock.systemUTC()).plusMinutes(10).truncatedTo(ChronoUnit.MINUTES).toEpochSecond,
      claims = ClaimsFixture.createRandom()
    )
