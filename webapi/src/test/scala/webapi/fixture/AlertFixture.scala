package webapi.fixture

import webapi.model.Alert

import testkit.core.createRandomKey
import testkit.core.createRandomName

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
