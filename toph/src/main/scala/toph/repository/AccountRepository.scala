package toph.repository

import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentUpdateOptions
import io.github.arainko.ducktape.Field
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer
import one.estrondo.farango.zio.given
import toph.model.Account
import toph.repository.AccountRepository.Update
import tremors.zio.farango.CollectionManager
import zio.Task
import zio.ZIO

trait AccountRepository:

  def add(user: Account): Task[Account]

  def update(key: String, update: Update): Task[Account]

  def searchByEmail(email: String): Task[Option[Account]]

object AccountRepository:

  def apply(collectionManager: CollectionManager): Task[AccountRepository] =
    ZIO.attempt(Impl(collectionManager))

  case class Update(name: String)

  case class Stored(_key: String, name: String, email: String)

  private given FarangoTransformer[Account, Stored] = DucktapeTransformer(Field.renamed(_._key, _.key))

  private given FarangoTransformer[Stored, Account] = DucktapeTransformer(Field.renamed(_.key, _._key))

  private class Impl(collectionManager: CollectionManager) extends AccountRepository:

    private def collection = collectionManager.collection

    private def database = collectionManager.database

    private val searchByEmailQuery =
      """FOR u IN @@collection
        | FILTER u.email == @email
        | LIMIT 1
        |RETURN u
        |""".stripMargin

    override def add(account: Account): Task[Account] =
      for entity <- collection
                      .insertDocument[Stored, Account](account, DocumentCreateOptions().returnNew(true))
                      .retry(collectionManager.sakePolicy)
      yield entity.getNew()

    override def searchByEmail(email: String): Task[Option[Account]] =
      database
        .query[Stored, Account](
          searchByEmailQuery,
          Map("@collection" -> collection.name, "email" -> email),
        )
        .runHead

    override def update(key: String, update: Update): Task[Account] =
      for entity <- collection
                      .updateDocument[Account, Update, Account](key, update, DocumentUpdateOptions().returnNew(true))
                      .retry(collectionManager.sakePolicy)
      yield entity.getNew()
