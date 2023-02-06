package toph.fixture

import toph.model.Hypocentre
import core.KeyGenerator
import toph.model.Point3D
import scala.util.Random
import toph.model.Uncertainty3D
import testkit.core.createZonedDateTime

object HypocentreFixture:

  def createRandom() = Hypocentre(
    key = KeyGenerator.next8(),
    position = Point3DFixture.createRandom(),
    positionUncertainty = Uncertainty3DFixture.createRandom(),
    time = createZonedDateTime(),
    timeUncertainty = Random.between(0, 30)
  )
