package toph.module

import toph.centre.UserCentre
import zio.Task

trait CentreModule:

  val userCentre: UserCentre

object CentreModule:

  def apply(): Task[CentreModule] = ???

  private class Impl(val userCentre: UserCentre) extends CentreModule
