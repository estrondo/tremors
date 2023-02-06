package toph.model

import io.github.arainko.ducktape.Transformer

object Uncertainty2D:

  given Transformer[Uncertainty2D, Array[Double]] = uncertainty2D => Array(uncertainty2D.lat, uncertainty2D.lng)
  given Transformer[Array[Double], Uncertainty2D] = array => Uncertainty2D(array(1), array(0))

case class Uncertainty2D(lng: Double, lat: Double)
