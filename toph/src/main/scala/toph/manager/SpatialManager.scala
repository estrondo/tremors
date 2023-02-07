package toph.manager

import com.softwaremill.macwire.wire
import quakeml.{Origin => QOrigin}
import toph.model.Epicentre
import toph.model.Hypocentre
import zio.Task

trait SpatialManager:

  def accept(origin: QOrigin): Task[(Epicentre, Hypocentre)]

object SpatialManager:

  def apply(): SpatialManager =
    wire[Impl]

  private class Impl() extends SpatialManager:

    override def accept(origin: QOrigin): Task[(Epicentre, Hypocentre)] = ???
