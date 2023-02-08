package toph.fixture

import core.KeyGenerator
import quakeml.RealQuantity
import quakeml.ResourceReference
import quakeml.TimeQuantity
import quakeml.{Origin => QOrigin}
import testkit.core.createZonedDateTime
import testkit.quakeml.RealQuantityFixture
import toph.model.Hypocentre
import toph.model.Point3D
import toph.model.Uncertainty3D

import scala.util.Random

object HypocentreFixture:

  def createRandom() = Hypocentre(
    key = KeyGenerator.next8(),
    position = Point3DFixture.createRandom(),
    positionUncertainty = Uncertainty3DFixture.createRandom(),
    time = createZonedDateTime(),
    timeUncertainty = Random.between(0, 30)
  )

  def updateWith(origin: QOrigin, hypocentre: Hypocentre): QOrigin =
    origin.copy(
      publicID = ResourceReference(hypocentre.key),
      latitude = RealQuantity(value = hypocentre.position.lat, uncertainty = Some(hypocentre.positionUncertainty.lat)),
      longitude = RealQuantity(value = hypocentre.position.lng, uncertainty = Some(hypocentre.positionUncertainty.lng)),
      depth = Some(RealQuantity(value = hypocentre.position.z, uncertainty = Some(hypocentre.positionUncertainty.z))),
      time = TimeQuantity(value = hypocentre.time, uncertainty = Some(hypocentre.timeUncertainty))
    )
