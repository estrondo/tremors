package toph.fixture

import toph.model.Epicentre
import core.KeyGenerator
import testkit.core.createZonedDateTime
import scala.util.Random

object EpicentreFixture:

  def createRandom() = Epicentre(
    key = KeyGenerator.next8(),
    position = Point2DFixture.createRandom(),
    positionUncertainty = Uncertainty2DFixture.createRandom(),
    time = createZonedDateTime(),
    timeUncertainty = Random.between(0, 10)
  )
