package toph.fixture

import org.locationtech.jts.geom.Coordinate
import toph.geom.Factory

import scala.util.Random

object PointFixture:

  def createRandom() = Factory.createPoint(Coordinate(Random.between(-180, 180), Random.between(-90, 90)))
