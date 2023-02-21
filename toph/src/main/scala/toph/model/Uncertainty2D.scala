package toph.model

import io.github.arainko.ducktape.Transformer
import quakeml.QuakeMLRealQuantity

object Uncertainty2D:

  given Transformer[Uncertainty2D, Array[Double]] = uncertainty2D => Array(uncertainty2D.lng, uncertainty2D.lat)
  given Transformer[Array[Double], Uncertainty2D] = array => Uncertainty2D(array(0), array(1))

  given Transformer[Uncertainty2D, Seq[Double]] = uncertainty2D => Array(uncertainty2D.lng, uncertainty2D.lat)
  given Transformer[Seq[Double], Uncertainty2D] = array => Uncertainty2D(array(0), array(1))

  def from(lng: QuakeMLRealQuantity, lat: QuakeMLRealQuantity): Uncertainty2D = Uncertainty2D(
    lng.uncertainty getOrElse 0d,
    lat.uncertainty getOrElse 0d
  )

case class Uncertainty2D(lng: Double, lat: Double)
