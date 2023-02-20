package webapi.model

object MagnitudeFilter:

  case class Range(min: Double, max: Double, orEqual: Boolean = true) extends MagnitudeFilter

  case class Less(value: Double, orEqual: Boolean = true) extends MagnitudeFilter

  case class Greater(value: Double, orEqual: Boolean = true) extends MagnitudeFilter

sealed trait MagnitudeFilter
