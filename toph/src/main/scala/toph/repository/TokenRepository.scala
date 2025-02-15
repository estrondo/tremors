package toph.repository

import com.arangodb.model.DocumentCreateOptions
import com.softwaremill.macwire.wire
import java.time.ZonedDateTime
import one.estrondo.farango.ducktape.given
import one.estrondo.farango.zio.given
import toph.model.Token
import tremors.zio.farango.CollectionManager
import zio.Task

trait TokenRepository:

  def add(token: Token): Task[Token]

  def get(key: String): Task[Option[Token]]

object TokenRepository:

  def apply(collectionManager: CollectionManager): TokenRepository =
    wire[Impl]

  private case class StoredToken(
      key: String,
      accessTokenHash: String,
      accountEmail: String,
      accessTokenExpiration: ZonedDateTime,
      origin: Option[String],
      device: String,
      createdAt: ZonedDateTime,
      expiration: ZonedDateTime,
      accountKey: String,
      accessToken: Array[Byte],
  )

  class Impl(collectionManager: CollectionManager) extends TokenRepository:

    private inline def collection = collectionManager.collection

    override def add(token: Token): Task[Token] =
      for entity <- collection
                      .insertDocument[StoredToken, Token](token, DocumentCreateOptions().returnNew(true))
                      .retry(collectionManager.sakePolicy)
      yield entity.getNew()

    override def get(key: String): Task[Option[Token]] =
      collection.getDocument[StoredToken, Token](key)
