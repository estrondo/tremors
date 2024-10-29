package toph.repository

import com.arangodb.model.DocumentCreateOptions
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.Transformer
import java.time.ZonedDateTime
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer
import one.estrondo.farango.zio.given
import org.locationtech.jts.geom.Point
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
      _key: String,
      eventId: String,
      preferredOriginId: Option[String],
      preferredMagnitude: Option[String],
      originId: String,
      originTime: ZonedDateTime,
      originLocation: Point,
      originUncertainty: Seq[Double],
      originDepth: Option[Double],
      originDepthUncertainty: Option[Double],
      originReferenceSystemId: Option[String],
      originMethodId: Option[String],
      originEarthModelId: Option[String],
      magnitudeId: String,
      magnitudeValue: Double,
      magnitudeUncertainty: Option[Double],
      magnitudeOriginId: Option[String],
      magnitudeMethodId: Option[String],
      magnitudeStationCount: Option[Int],
      magnitudeEvaluationMode: Option[String],
      magnitudeEvaluationStatus: Option[String],
  )

  private given FarangoTransformer[TophEvent, Stored] = DucktapeTransformer(
    Field.renamed(_._key, _.id),
  )

  private given FarangoTransformer[Stored, TophEvent] = DucktapeTransformer(
    Field.renamed(_.id, _._key),
  )

  private class Impl(collectionManager: CollectionManager) extends EventRepository:

    private val retryPolicy = collectionManager.sakePolicy

    private def collection = collectionManager.collection

    override def add(event: TophEvent): Task[TophEvent] =
      for entity <- collection
                      .insertDocument[Stored, TophEvent](event, DocumentCreateOptions().returnNew(true))
                      .retry(retryPolicy)
      yield entity.getNew()
