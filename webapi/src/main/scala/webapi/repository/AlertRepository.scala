package webapi.repository

import com.arangodb.model.GeoIndexOptions
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import farango.DocumentCollection
import farango.UpdateReturn
import farango.data.Key
import farango.data.given
import farango.zio.given
import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.into
import org.locationtech.jts.geom.MultiPolygon
import webapi.model.Alert
import webapi.model.Alert.Update
import webapi.model.Location
import webapi.model.Location.City
import webapi.model.Location.Country
import webapi.model.Location.Region
import webapi.model.MagnitudeFilter
import webapi.model.MagnitudeFilter.Greater
import webapi.model.MagnitudeFilter.Less
import webapi.model.MagnitudeFilter.Range
import zio.Task
import zio.ZIO
import zio.stream.ZStream

trait AlertRepository:

  def all(): Task[ZStream[Any, Throwable, Alert]]

  def add(alert: Alert): Task[Alert]

  def enable(key: String, enabled: Boolean): Task[Option[Alert]]

  def update(key: String, update: Alert.Update): Task[Option[Alert]]

  def remove(key: String): Task[Option[Alert]]

object AlertRepository:

  def apply(collection: DocumentCollection): Task[AlertRepository] =
    for _ <- collection.ensureGeoIndex(Seq("area"), GeoIndexOptions().geoJson(true))
    yield Impl(collection)

  @JsonTypeInfo(
    use = Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
  )
  @JsonSubTypes(
    Array(
      Type(value = classOf[LocationCityDocument], name = "city"),
      Type(value = classOf[LocationCountryDocument], name = "country"),
      Type(value = classOf[LocationRegionDocument], name = "region")
    )
  )
  sealed trait LocationDocument
  case class LocationCountryDocument(name: String)                 extends LocationDocument
  case class LocationCityDocument(name: String, country: String)   extends LocationDocument
  case class LocationRegionDocument(name: String, country: String) extends LocationDocument

  private[repository] given Conversion[Location, LocationDocument] = {
    case City(name, country)   => LocationCityDocument(name, country)
    case Country(name)         => LocationCountryDocument(name)
    case Region(name, country) => LocationRegionDocument(name, country)
  }

  private[repository] given Conversion[LocationDocument, Location] = {
    case LocationCityDocument(name, country)   => City(name, country)
    case LocationCountryDocument(name)         => Country(name)
    case LocationRegionDocument(name, country) => Region(name, country)
  }

  @JsonTypeInfo(
    use = Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
  )
  @JsonSubTypes(
    Array(
      Type(value = classOf[MagnitudeFilterDocumentLess], name = "less"),
      Type(value = classOf[MagnitudeFilterDocumentGreater], name = "greater"),
      Type(value = classOf[MagnitudeFilterDocumentRange], name = "range")
    )
  )
  sealed trait MagnitudeFilterDocument
  case class MagnitudeFilterDocumentRange(min: Double, max: Double, orEqual: Boolean = true)
      extends MagnitudeFilterDocument
  case class MagnitudeFilterDocumentLess(value: Double, orEqual: Boolean = true)    extends MagnitudeFilterDocument
  case class MagnitudeFilterDocumentGreater(value: Double, orEqual: Boolean = true) extends MagnitudeFilterDocument

  private[repository] given Conversion[MagnitudeFilter, MagnitudeFilterDocument] = {
    case Greater(value, orEqual)  => MagnitudeFilterDocumentGreater(value, orEqual)
    case Less(value, orEqual)     => MagnitudeFilterDocumentLess(value, orEqual)
    case Range(min, max, orEqual) => MagnitudeFilterDocumentRange(min, max, orEqual)
  }

  private[repository] given Conversion[MagnitudeFilterDocument, MagnitudeFilter] = {
    case MagnitudeFilterDocumentGreater(value, orEqual)  => Greater(value, orEqual)
    case MagnitudeFilterDocumentLess(value, orEqual)     => Less(value, orEqual)
    case MagnitudeFilterDocumentRange(min, max, orEqual) => Range(min, max, orEqual)
  }

  private[repository] case class Document(
      _key: Key,
      email: String,
      enabled: Boolean,
      area: Option[MultiPolygon],
      areaRadius: Option[Int],
      location: Seq[LocationDocument],
      magnitudeFilter: Seq[MagnitudeFilterDocument]
  )

  private[repository] given Conversion[Alert, Document] = alert =>
    alert
      .into[Document]
      .transform(
        Field.const(_._key, alert.key: Key)
      )

  private[repository] given Conversion[Document, Alert] = document =>
    document
      .into[Alert]
      .transform(
        Field.const(_.key, document._key: String)
      )

  private[repository] case class UpdateDocument(
      area: Option[MultiPolygon],
      areaRadius: Option[Int],
      magnitudeFilter: Seq[MagnitudeFilterDocument],
      location: Seq[LocationDocument]
  )

  private[repository] given Conversion[Alert.Update, UpdateDocument] = update =>
    update
      .into[UpdateDocument]
      .transform()

  private class UpdateEnabled(val enabled: Boolean)

  private class Impl(collection: DocumentCollection) extends AlertRepository:

    override def add(alert: Alert): Task[Alert] =
      collection
        .insert[Document](alert)
        .tap(_ => ZIO.logDebug("An alert was added."))
        .tapErrorCause(ZIO.logErrorCause("It was impossible to add an alert!", _))

    override def all(): Task[ZStream[Any, Throwable, Alert]] =
      ZIO.attempt(collection.documents[Document]())

    override def enable(key: String, enabled: Boolean): Task[Option[Alert]] =
      collection
        .updateT[UpdateEnabled, Document, [X] =>> ZIO[Any, Throwable, X]](
          Key.safe(key),
          UpdateEnabled(enabled),
          UpdateReturn.New
        )
        .map(_.map(x => x: Alert))
        .tap(_ => ZIO.logDebug(s"Alert $key was changed to enable=$enabled."))
        .tapErrorCause(ZIO.logErrorCause(s"It was impossible enable/disable alert $key!", _))

    override def update(key: String, update: Update): Task[Option[Alert]] =
      collection
        .update[UpdateDocument, Document](Key.safe(key), update)
        .tap(_ => ZIO.logDebug("An alert was updated."))
        .tapErrorCause(ZIO.logErrorCause("It was impossible to update an alert!", _))

    override def remove(key: String): Task[Option[Alert]] =
      collection
        .remove[Document](Key.safe(key))
        .tap(_ => ZIO.logDebug("An alert was removed."))
        .tapErrorCause(ZIO.logErrorCause("It was impossible to remove alert=$key!", _))
