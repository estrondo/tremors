package toph.fixture

import quakeml.QuakeMLOrigin
import quakeml.QuakeMLRealQuantity
import quakeml.QuakeMLResourceReference
import quakeml.QuakeMLTimeQuantity
import scala.util.Random
import testkit.core.createRandomResourceID
import testkit.core.createZonedDateTime
import toph.model.data.HypocentreData

object HypocentreDataFixture:

  def createRandom() = HypocentreData(
    key = createRandomResourceID(),
    position = PointFixture.createRandom(),
    depth = Some(Random.between(0, 7500)),
    depthUncertainty = Some(Random.between(0, 15000)),
    positionUncertainty = Uncertainty2DFixture.createRandom(),
    time = createZonedDateTime(),
    timeUncertainty = Random.between(0, 30)
  )

  def updateOriginWith(origin: QuakeMLOrigin, hypocentre: HypocentreData): QuakeMLOrigin =
    origin.copy(
      publicID = QuakeMLResourceReference(hypocentre.key),
      latitude =
        QuakeMLRealQuantity(value = hypocentre.position.getY(), uncertainty = Some(hypocentre.positionUncertainty.lat)),
      longitude =
        QuakeMLRealQuantity(value = hypocentre.position.getX(), uncertainty = Some(hypocentre.positionUncertainty.lng)),
      depth = hypocentre.depth.map(depth => QuakeMLRealQuantity(depth, hypocentre.depthUncertainty)),
      time = QuakeMLTimeQuantity(value = hypocentre.time, uncertainty = Some(hypocentre.timeUncertainty))
    )
