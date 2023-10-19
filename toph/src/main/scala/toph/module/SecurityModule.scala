package toph.module

import toph.centre.SecurityCentre
import zio.Task
import zio.ZIO

trait SecurityModule:

  val securityCentre: SecurityCentre

object SecurityModule:

  def apply(centreModule: CentreModule): Task[SecurityModule] =
    ???

  private class Impl(val securityCentre: SecurityCentre) extends SecurityModule
