package toph.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.Key
import farango.data.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.CreationInfo
import toph.model.Magnitude
import zio.Task

import java.lang.{Double => JDouble}
import java.lang.{Long => JLong}
import java.time.ZonedDateTime

trait MagnitudeRepository:

  def add(magnitude: Magnitude): Task[Magnitude]

  def get(key: String): Task[Option[Magnitude]]

  def remove(key: String): Task[Option[Magnitude]]

object MagnitudeRepository:

  def apply(collection: DocumentCollection): MagnitudeRepository =
    wire[Impl]

  private[repository] case class Document(
      _key: Key,
      mag: Double,
      azimuthalGap: Option[Double],
      creationInfo: Option[CreationInfoDocument],
      stationCount: Option[Int],
      comment: Seq[String],
      `type`: Option[String],
      evaluationMode: Option[String],
      methodID: Option[String],
      evaluationStatus: Option[String],
      originID: Option[String]
  )

  private[repository] case class CreationInfoDocument(
      author: Option[String],
      version: Option[String],
      agencyID: Option[String],
      agencyURI: Option[String],
      creationTime: Option[ZonedDateTime]
  )

  private[repository] given Conversion[CreationInfoDocument, CreationInfo] = document =>
    document
      .into[CreationInfo]
      .transform()

  private[repository] given Conversion[CreationInfo, CreationInfoDocument] = creationInfo =>
    creationInfo
      .into[CreationInfoDocument]
      .transform()

  private[repository] given Conversion[Magnitude, Document] = magnitude =>
    magnitude
      .into[Document]
      .transform(Field.const(_._key, magnitude.key: Key))

  private[repository] given Conversion[Document, Magnitude] = document =>
    document
      .into[Magnitude]
      .transform(Field.const(_.key, document._key: String))

  private class Impl(collection: DocumentCollection) extends MagnitudeRepository:

    override def add(magnitude: Magnitude): Task[Magnitude] =
      collection
        .insert[Document](magnitude)

    override def get(key: String): Task[Option[Magnitude]] =
      collection.get[Document](Key.safe(key))

    override def remove(key: String): Task[Option[Magnitude]] =
      collection.remove[Document](Key.safe(key))
