package toph.fixture

import toph.model.Uncertainty3D
import scala.util.Random

object Uncertainty3DFixture:

  def createRandom() = Uncertainty3D(Random.between(0d, 1d), Random.between(0d, 1d), Random.between(0d, 100d))
