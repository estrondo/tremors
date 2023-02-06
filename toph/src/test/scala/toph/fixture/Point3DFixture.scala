package toph.fixture

import toph.model.Point3D
import scala.util.Random

object Point3DFixture:

  def createRandom() = Point3D(Random.between(-180d, 180), Random.between(-90d, 90d), Random.between(0d, 100000d))
