package webapi.fixture

import webapi.model.User
import testkit.core.createRandomName
import testkit.core.createZonedDateTime

object UserFixture:

  def createRandom() =
    val name = createRandomName()
    User(
      name = name,
      email = s"$name@estrondo.one",
      createdAt = createZonedDateTime()
    )
