package toph.model

import io.github.arainko.ducktape.Transformer

object Uncertainty3D:

  given Transformer[Uncertainty3D, Array[Double]] with
    override def transform(from: Uncertainty3D): Array[Double] = Array(from.lat, from.lng, from.z)

  given Transformer[Array[Double], Uncertainty3D] with
    override def transform(from: Array[Double]): Uncertainty3D = Uncertainty3D(from(1), from(0), from(2))

case class Uncertainty3D(lng: Double, lat: Double, z: Double)
