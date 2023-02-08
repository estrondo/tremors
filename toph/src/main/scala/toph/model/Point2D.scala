package toph.model

import io.github.arainko.ducktape.Transformer
import quakeml.RealQuantity

object Point2D:

  given Transformer[Point2D, Array[Double]] = point2D => Array(point2D.lat, point2D.lng)
  given Transformer[Array[Double], Point2D] = array => Point2D(array(1), array(0))

  def from(lng: RealQuantity, lat: RealQuantity): Point2D = Point2D(lng.value, lat.value)

case class Point2D(lng: Double, lat: Double)
