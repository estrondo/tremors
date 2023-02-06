package toph.fixture

import toph.model.Event
import core.KeyGenerator

object EventFixture:

  def createRandom() = Event(
    key = KeyGenerator.next8()
  )
