package toph.security

import java.util.UUID
import toph.model.AccountFixture

object TokenFixture:

  def createRandom(): Token = Token(
    account = AccountFixture.createRandom(),
    token = UUID.randomUUID().toString,
  )
