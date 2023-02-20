package webapi.model

object Location:

  case class Country(name: String) extends Location

  case class City(name: String, country: String) extends Location

  case class Region(name: String, country: String) extends Location

sealed trait Location
