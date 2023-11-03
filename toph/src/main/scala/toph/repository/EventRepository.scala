package toph.repository

import com.arangodb.model.DocumentCreateOptions
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.Transformer
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer
import one.estrondo.farango.zio.given
import toph.model.TophEvent
import tremors.zio.farango.CollectionManager
import zio.Task
import zio.ZIO

trait EventRepository:

  def add(event: TophEvent): Task[TophEvent]

object EventRepository:

  def apply(collectionManager: CollectionManager): Task[EventRepository] =
    ZIO.succeed(Impl(collectionManager))

  case class Stored(
      _key: String
  )

  private given FarangoTransformer[TophEvent, Stored] = DucktapeTransformer(
    Field.renamed(_._key, _.id)
  )

  private given FarangoTransformer[Stored, TophEvent] = DucktapeTransformer(
    Field.renamed(_.id, _._key)
  )

  private class Impl(collectionManager: CollectionManager) extends EventRepository:

    private val retryPolicy = collectionManager.sakePolicy

    private def collection = collectionManager.collection

    override def add(event: TophEvent): Task[TophEvent] =
      for entity <- collection
                      .insertDocument[Stored, TophEvent](event, DocumentCreateOptions().returnNew(true))
                      .retry(retryPolicy)
      yield entity.getNew()
