package toph.security

import scala.util.Random
import toph.model.AccountFixture
import tremors.generator.KeyGenerator

object AuthorisedAccessFixture:

  def createRandom(): AuthorisedAccess = AuthorisedAccess(
    account = AccountFixture.createRandom(),
    accessToken = Random.nextBytes(128),
    refreshToken = KeyGenerator.long(),
  )
