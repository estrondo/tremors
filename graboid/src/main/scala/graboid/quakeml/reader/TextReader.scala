package graboid.quakeml.reader

import tremors.quakeml.ResourceReference

trait TextReader[+T]:
  def apply(text: String): T

object TextReader:

  given TextReader[ResourceReference] with
    override def apply(text: String): ResourceReference = ResourceReference(text)

  given TextReader[String] with

    override def apply(text: String): String = text

  given TextReader[Boolean] with

    override def apply(text: String): Boolean = text.toLowerCase == "true"

  given TextReader[Int] with

    override def apply(text: String): Int = text.toInt

  given TextReader[Double] with

    override def apply(text: String): Double = text.toDouble
