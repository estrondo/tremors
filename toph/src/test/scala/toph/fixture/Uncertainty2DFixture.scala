package toph.fixture

import toph.model.Uncertainty2D
import scala.util.Random

object Uncertainty2DFixture:
  def createRandom() = Uncertainty2D(Random.between(-180d, 180), Random.between(-90d, 90))
