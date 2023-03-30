package toph.fixture

import org.locationtech.jts.geom.Coordinate
import scala.util.Random
import toph.geom.GeometryFactory

object PointFixture:

  def createRandom() = GeometryFactory.createPoint(Coordinate(Random.between(-180, 180), Random.between(-90, 90)))
