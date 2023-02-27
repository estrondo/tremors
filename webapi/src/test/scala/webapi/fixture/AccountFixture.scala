package webapi.fixture

import core.KeyGenerator
import testkit.core.createRandomName
import testkit.core.createZonedDateTime
import webapi.model.Account

object AccountFixture:

  def createRandom() =
    val name = createRandomName()
    Account(
      name = name,
      email = s"$name@estrondo.one",
      createdAt = createZonedDateTime(),
      active = true,
      secret = KeyGenerator.next4()
    )
