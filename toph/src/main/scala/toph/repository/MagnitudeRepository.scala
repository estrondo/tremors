package toph.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.ArangoConversion.convertFromKey
import farango.data.ArangoConversion.convertToKey
import farango.data.ArangoConversion.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import toph.model.CreationInfo
import toph.model.Magnitude
import zio.Task

import java.lang.{Double => JDouble}
import java.lang.{Long => JLong}

trait MagnitudeRepository:

  def add(magnitude: Magnitude): Task[Magnitude]

  def get(key: String): Task[Option[Magnitude]]

  def remove(key: String): Task[Option[Magnitude]]

object MagnitudeRepository:

  def apply(collection: DocumentCollection): MagnitudeRepository =
    wire[Impl]

  private[repository] case class Document(
      _key: String,
      mag: Double,
      azimuthalGap: JDouble,
      creationInfo: CreationInfoDocument,
      stationCount: Integer,
      comment: Seq[String],
      `type`: String,
      evaluationMode: String,
      methodID: String,
      evaluationStatus: String,
      originID: String
  )

  private[repository] case class CreationInfoDocument(
      author: String,
      version: String,
      agencyID: String,
      agencyURI: String,
      creationTime: JLong
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
      .transform(Field.const(_._key, convertToKey(magnitude.key)))

  private[repository] given Conversion[Document, Magnitude] = document =>
    document
      .into[Magnitude]
      .transform(Field.const(_.key, convertFromKey(document._key)))

  private class Impl(collection: DocumentCollection) extends MagnitudeRepository:

    override def add(magnitude: Magnitude): Task[Magnitude] =
      collection
        .insert[Document](magnitude)

    override def get(key: String): Task[Option[Magnitude]] =
      collection.get[Document](convertToKey(key))

    override def remove(key: String): Task[Option[Magnitude]] =
      collection.remove[Document](convertToKey(key))
