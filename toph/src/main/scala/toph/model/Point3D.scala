package toph.model

import io.github.arainko.ducktape.Transformer

object Point3D:

  given Transformer[Point3D, Array[Double]] with
    override def transform(from: Point3D): Array[Double] = Array(from.lat, from.lng, from.z)

  given Transformer[Array[Double], Point3D] with
    override def transform(from: Array[Double]): Point3D = Point3D(from(1), from(0), from(2))

case class Point3D(lng: Double, lat: Double, z: Double)
