package toph.repository

import com.softwaremill.macwire.wire
import farango.DocumentCollection
import farango.data.Key
import farango.data.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import java.lang.{Double => JDouble}
import java.lang.{Long => JLong}
import java.time.ZonedDateTime
import toph.model.data.CreationInfoData
import toph.model.data.MagnitudeData
import zio.Task

trait MagnitudeDataRepository:

  def add(magnitude: MagnitudeData): Task[MagnitudeData]

  def get(key: String): Task[Option[MagnitudeData]]

  def remove(key: String): Task[Option[MagnitudeData]]

object MagnitudeDataRepository:

  def apply(collection: DocumentCollection): MagnitudeDataRepository =
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

  private[repository] given Conversion[CreationInfoDocument, CreationInfoData] = document =>
    document
      .into[CreationInfoData]
      .transform()

  private[repository] given Conversion[CreationInfoData, CreationInfoDocument] = creationInfo =>
    creationInfo
      .into[CreationInfoDocument]
      .transform()

  private[repository] given Conversion[MagnitudeData, Document] = magnitude =>
    magnitude
      .into[Document]
      .transform(Field.const(_._key, magnitude.key: Key))

  private[repository] given Conversion[Document, MagnitudeData] = document =>
    document
      .into[MagnitudeData]
      .transform(Field.const(_.key, document._key: String))

  private class Impl(collection: DocumentCollection) extends MagnitudeDataRepository:

    override def add(magnitude: MagnitudeData): Task[MagnitudeData] =
      collection
        .insert[Document](magnitude)

    override def get(key: String): Task[Option[MagnitudeData]] =
      collection.get[Document](Key.safe(key))

    override def remove(key: String): Task[Option[MagnitudeData]] =
      collection.remove[Document](Key.safe(key))
