package webapi.fixture

import testkit.core.createRandomKey
import testkit.core.createRandomName
import webapi.model.Alert

object AlertFixture:

  def createRandom() = Alert(
    key = createRandomKey(),
    email = s"${createRandomName()}@estrondo.one",
    enabled = true,
    area = None,
    areaRadius = None,
    magnitudeFilter = Nil,
    location = Nil
  )
