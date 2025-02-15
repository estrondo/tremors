package toph.security

import java.util.UUID
import toph.model.AccountFixture

object AccessTokenFixture:

  def createRandom(): AccessToken = AccessToken(
    account = AccountFixture.createRandom(),
    token = UUID.randomUUID().toString.getBytes,
  )
