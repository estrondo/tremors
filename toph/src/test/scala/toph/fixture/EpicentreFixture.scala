package toph.fixture

import core.KeyGenerator
import org.locationtech.jts.geom.Coordinate
import testkit.core.createRandomResourceID
import testkit.core.createZonedDateTime
import toph.geom.Factory
import toph.model.Epicentre
import toph.model.Hypocentre
import toph.model.Uncertainty2D

import scala.util.Random

object EpicentreFixture:

  def createRandom() = Epicentre(
    key = createRandomResourceID(),
    position = PointFixture.createRandom(),
    positionUncertainty = Uncertainty2DFixture.createRandom(),
    time = createZonedDateTime(),
    timeUncertainty = Random.between(0, 10)
  )

  def from(hypocentre: Hypocentre) = Epicentre(
    key = hypocentre.key,
    position = Factory.createPoint(hypocentre.position.getCoordinate()),
    positionUncertainty = Uncertainty2D(hypocentre.positionUncertainty.lng, hypocentre.positionUncertainty.lat),
    time = hypocentre.time,
    timeUncertainty = hypocentre.timeUncertainty
  )
