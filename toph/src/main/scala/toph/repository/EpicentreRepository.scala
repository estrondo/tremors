package toph.repository

import com.arangodb.model.GeoIndexOptions
import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.Epicentre
import zio.Task
import zio.ZIO

import java.time.ZonedDateTime
import farango.data.Key

trait EpicentreRepository:

  def add(epicentre: Epicentre): Task[Epicentre]

  def remove(key: String): Task[Option[Epicentre]]

object EpicentreRepository:

  def apply(collection: DocumentCollection): Task[EpicentreRepository] =
    for
      indexEntry <- collection.ensureGeoIndex(Seq("position"), GeoIndexOptions().geoJson(false))
      a          <- ZIO.logDebug(s"GeoIndex ${indexEntry.getName()} was created for collection ${collection.name}.")
    yield wire[Impl]

  private[repository] case class Document(
      _key: Key,
      position: Array[Double],
      positionUncertainty: Array[Double],
      time: ZonedDateTime,
      timeUncertainty: Int
  )

  private[repository] given Conversion[Epicentre, Document] = epicentre =>
    epicentre
      .into[Document]
      .transform(Field.const(_._key, epicentre.key: Key))

  private[repository] given Conversion[Document, Epicentre] = document =>
    document
      .into[Epicentre]
      .transform(Field.const(_.key, document._key: String))

  private class Impl(collection: DocumentCollection) extends EpicentreRepository:

    override def add(epicentre: Epicentre): Task[Epicentre] =
      collection
        .insert[Document](epicentre)
        .tap(_ => ZIO.logDebug(s"A new epicentre was added: ${epicentre.key}."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add an epicentre: ${epicentre.key}!", _))

    override def remove(key: String): Task[Option[Epicentre]] =
      collection
        .remove[Document](Key.safe(key))
        .tap(_ => ZIO.logDebug(s"An epicentre was removed: $key."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to remove an epicentre: $key!", _))
