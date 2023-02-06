package toph.model

import io.github.arainko.ducktape.Transformer

object Point2D:

  given Transformer[Point2D, Array[Double]] = point2D => Array(point2D.lat, point2D.lng)
  given Transformer[Array[Double], Point2D] = array => Point2D(array(1), array(0))

case class Point2D(lng: Double, lat: Double)
