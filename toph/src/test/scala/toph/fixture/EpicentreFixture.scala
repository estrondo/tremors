package toph.fixture

import core.KeyGenerator
import testkit.core.createZonedDateTime
import toph.model.Epicentre
import toph.model.Hypocentre
import toph.model.Point2D
import toph.model.Uncertainty2D

import scala.util.Random

object EpicentreFixture:

  def createRandom() = Epicentre(
    key = KeyGenerator.next8(),
    position = Point2DFixture.createRandom(),
    positionUncertainty = Uncertainty2DFixture.createRandom(),
    time = createZonedDateTime(),
    timeUncertainty = Random.between(0, 10)
  )

  def from(hypocentre: Hypocentre) = Epicentre(
    key = hypocentre.key,
    position = Point2D(hypocentre.position.lng, hypocentre.position.lat),
    positionUncertainty = Uncertainty2D(hypocentre.positionUncertainty.lng, hypocentre.positionUncertainty.lat),
    time = hypocentre.time,
    timeUncertainty = hypocentre.timeUncertainty
  )