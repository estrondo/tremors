package toph.fixture

import toph.model.Point2D
import scala.util.Random

object Point2DFixture:
  def createRandom() = Point2D(Random.between(-180d, 180), Random.between(-90d, 90d))
