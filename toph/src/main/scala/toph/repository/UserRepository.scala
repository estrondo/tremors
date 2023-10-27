package toph.repository

import toph.model.TophUser
import toph.repository.UserRepository.Update
import tremors.zio.farango.CollectionManager
import zio.Task
import zio.ZIO

trait UserRepository:

  def update(id: String, update: Update): Task[TophUser]

object UserRepository:

  def apply(collectionManager: CollectionManager): Task[UserRepository] =
    ZIO.attempt(Impl(collectionManager))

  case class Update(name: String)

  private class Impl(collectionManager: CollectionManager) extends UserRepository:

    private val collection = collectionManager.collection

    override def update(id: String, update: Update): Task[TophUser] = ???
