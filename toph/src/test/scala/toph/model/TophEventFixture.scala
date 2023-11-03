package toph.model

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength
import tremors.quakeml.Event

object TophEventFixture:

  def createRandom(): TophEvent = TophEvent(
    id = KeyGenerator.generate(KeyLength.Long)
  )

  def createRandom(source: Event): TophEvent = TophEvent(
    id = KeyGenerator.generate(KeyLength.Medium)
  )
