package toph.fixture

import scala.util.Random
import toph.model.Uncertainty2D

object Uncertainty2DFixture:
  def createRandom() = Uncertainty2D(Random.between(0d, 1d), Random.between(0, 1d))
