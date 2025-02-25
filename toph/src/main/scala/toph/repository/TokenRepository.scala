package toph.repository

import com.arangodb.model.DocumentCreateOptions
import com.softwaremill.macwire.wire
import io.github.arainko.ducktape.Field
import java.time.ZonedDateTime
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer
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

  private case class Stored(
      _key: String,
      accessTokenHash: String,
      accountEmail: String,
      accessTokenExpiration: ZonedDateTime,
      origin: Option[String],
      device: String,
      createdAt: ZonedDateTime,
      expiration: ZonedDateTime,
      accountKey: String,
  )

  private given FarangoTransformer[Stored, Token] = DucktapeTransformer(
    Field.renamed(_.key, _._key),
  )

  private given FarangoTransformer[Token, Stored] = DucktapeTransformer(
    Field.renamed(_._key, _.key),
  )

  class Impl(collectionManager: CollectionManager) extends TokenRepository:

    private inline def collection = collectionManager.collection

    override def add(token: Token): Task[Token] =
      for entity <- collection
                      .insertDocument[Stored, Token](token, DocumentCreateOptions().returnNew(true))
                      .retry(collectionManager.sakePolicy)
      yield entity.getNew()

    override def get(key: String): Task[Option[Token]] =
      collection.getDocument[Stored, Token](key)
