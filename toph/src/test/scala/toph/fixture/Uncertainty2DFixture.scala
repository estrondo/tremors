package toph.fixture

import toph.model.Uncertainty2D

import scala.util.Random

object Uncertainty2DFixture:
  def createRandom() = Uncertainty2D(Random.between(0d, 1d), Random.between(0, 1d))
