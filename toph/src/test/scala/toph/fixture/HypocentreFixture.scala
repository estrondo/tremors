package toph.fixture

import core.KeyGenerator
import quakeml.RealQuantity
import quakeml.ResourceReference
import quakeml.TimeQuantity
import quakeml.{Origin => QOrigin}
import testkit.core.createRandomResourceID
import testkit.core.createZonedDateTime
import testkit.quakeml.RealQuantityFixture
import toph.model.Hypocentre
import toph.model.Uncertainty3D

import scala.util.Random

object HypocentreFixture:

  def createRandom() = Hypocentre(
    key = createRandomResourceID(),
    position = PointFixture.createRandom(),
    depth = Random.between(0, 7),
    positionUncertainty = Uncertainty3DFixture.createRandom(),
    time = createZonedDateTime(),
    timeUncertainty = Random.between(0, 30)
  )

  def updateOriginWith(origin: QOrigin, hypocentre: Hypocentre): QOrigin =
    origin.copy(
      publicID = ResourceReference(hypocentre.key),
      latitude =
        RealQuantity(value = hypocentre.position.getY(), uncertainty = Some(hypocentre.positionUncertainty.lat)),
      longitude =
        RealQuantity(value = hypocentre.position.getX(), uncertainty = Some(hypocentre.positionUncertainty.lng)),
      depth = Some(RealQuantity(value = hypocentre.depth, uncertainty = Some(hypocentre.positionUncertainty.z))),
      time = TimeQuantity(value = hypocentre.time, uncertainty = Some(hypocentre.timeUncertainty))
    )
