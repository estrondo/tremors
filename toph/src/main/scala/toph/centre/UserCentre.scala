package toph.centre

import toph.model.TophUser
import zio.Task

trait UserCentre:

  def update(id: String, update: UserCentre.Update): Task[TophUser]

object UserCentre:

  case class Update(name: String)
