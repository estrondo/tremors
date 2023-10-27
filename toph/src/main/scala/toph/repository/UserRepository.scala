package toph.repository

import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentUpdateOptions
import io.github.arainko.ducktape.Field
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer
import one.estrondo.farango.zio.given
import toph.model.TophUser
import toph.repository.UserRepository.Update
import tremors.zio.farango.CollectionManager
import zio.Task
import zio.ZIO

trait UserRepository:

  def add(user: TophUser): Task[TophUser]

  def update(id: String, update: Update): Task[TophUser]

  def searchByEmail(email: String): Task[Option[TophUser]]

object UserRepository:

  def apply(collectionManager: CollectionManager): Task[UserRepository] =
    ZIO.attempt(Impl(collectionManager))

  case class Update(name: String)

  case class Stored(_key: String, name: String, email: String)

  private given FarangoTransformer[TophUser, Stored] = DucktapeTransformer(Field.renamed(_._key, _.id))

  private given FarangoTransformer[Stored, TophUser] = DucktapeTransformer(Field.renamed(_.id, _._key))

  private class Impl(collectionManager: CollectionManager) extends UserRepository:

    private def collection = collectionManager.collection

    private def database = collectionManager.database

    private val searchByEmailQuery =
      """FOR u IN @@collection
        | FILTER u.email == @email
        | LIMIT 1
        |RETURN u
        |""".stripMargin

    override def add(user: TophUser): Task[TophUser] =
      for entity <- collection
                      .insertDocument[Stored, TophUser](user, DocumentCreateOptions().returnNew(true))
                      .retry(collectionManager.sakePolicy)
      yield entity.getNew()

    override def searchByEmail(email: String): Task[Option[TophUser]] =
      database
        .query[Stored, TophUser](
          searchByEmailQuery,
          Map("@collection" -> collection.name, "email" -> email)
        )
        .runHead

    override def update(id: String, update: Update): Task[TophUser] =
      for entity <- collection
                      .updateDocument[TophUser, Update, TophUser](id, update, DocumentUpdateOptions().returnNew(true))
                      .retry(collectionManager.sakePolicy)
      yield entity.getNew()
