package toph.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.ArangoConversion.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.Epicentre
import zio.Task
import zio.ZIO
import com.arangodb.model.GeoIndexOptions

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
      _key: String,
      position: Array[Double],
      positionUncertainty: Array[Double],
      time: Long,
      timeUncertainty: Int
  )

  private[repository] given Conversion[Epicentre, Document] =
    _.into[Document]
      .transform(Field.renamed(_._key, _.key))

  private[repository] given Conversion[Document, Epicentre] =
    _.into[Epicentre]
      .transform(Field.renamed(_.key, _._key))

  private class Impl(collection: DocumentCollection) extends EpicentreRepository:

    override def add(epicentre: Epicentre): Task[Epicentre] =
      collection
        .insert[Document](epicentre)
        .tap(_ => ZIO.logDebug(s"A new epicentre was added: ${epicentre.key}."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to add an epicentre: ${epicentre.key}!", _))

    override def remove(key: String): Task[Option[Epicentre]] =
      collection
        .remove[Document](key)
        .tap(_ => ZIO.logDebug(s"An epicentre was removed: $key."))
        .tapErrorCause(ZIO.logWarningCause(s"It was impossible to remove an epicentre: $key!", _))
