package tremors.quakeml

import io.bullet.borer.Codec
import io.bullet.borer.Decoder
import io.bullet.borer.Encoder
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodec
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveEncoder
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime

given Codec[ZonedDateTime] =
  val zoneId  = Clock.systemUTC().getZone
  val encoder = Encoder[Long].contramap[ZonedDateTime](_.toEpochSecond)
  val decoder = Decoder[Long].map(epochSecond => ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), zoneId))
  Codec(encoder, decoder)

case class Comment(
    text: String,
    id: Option[ResourceReference],
    creationInfo: Option[CreationInfo]
)

case class CompositeTime(
    year: Option[IntegerQuantity],
    month: Option[IntegerQuantity],
    day: Option[IntegerQuantity],
    hour: Option[IntegerQuantity],
    minute: Option[IntegerQuantity],
    second: Option[IntegerQuantity]
)
case class CreationInfo(
    agencyId: Option[String],
    agencyUri: Option[ResourceReference],
    author: Option[String],
    authorUri: Option[ResourceReference],
    creationTime: Option[ZonedDateTime],
    version: Option[String]
)
case class Event(
    publicId: ResourceReference,
    preferredOriginId: Option[ResourceReference],
    preferredMagnitudeId: Option[ResourceReference],
    preferredFocalMechanismId: Option[ResourceReference],
    `type`: Option[String],
    typeUncertainty: Option[String],
    description: Seq[EventDescription],
    comment: Seq[Comment],
    creationInfo: Option[CreationInfo],
    origin: Seq[Origin],
    magnitude: Seq[Magnitude]
)

case class EventDescription(
    text: String,
    `type`: Option[String]
)
case class IntegerQuantity(
    value: Int,
    uncertainty: Option[Int]
)

case class Magnitude(
    publicId: ResourceReference,
    mag: RealQuantity,
    `type`: Option[String],
    originId: Option[ResourceReference],
    methodId: Option[ResourceReference],
    stationCount: Option[Int],
    azimuthalGap: Option[Double],
    evaluationMode: Option[String],
    evaluationStatus: Option[String],
    comment: Seq[Comment],
    creationInfo: Option[CreationInfo]
)

case class Origin(
    publicId: ResourceReference,
    time: TimeQuantity,
    longitude: RealQuantity,
    latitude: RealQuantity,
    depth: Option[RealQuantity],
    depthType: Option[String],
    timeFixed: Option[Boolean],
    epicenterFixed: Option[Boolean],
    referenceSystemId: Option[ResourceReference],
    methodId: Option[ResourceReference],
    earthModelId: Option[ResourceReference],
    compositeTime: Option[CompositeTime],
    quality: Option[OriginQuality],
    `type`: Option[String],
    region: Option[String],
    evaluationMode: Option[String],
    evaluationStatus: Option[String],
    comment: Seq[Comment],
    creationInfo: Option[CreationInfo]
)

case class OriginQuality(
    associatedPhaseCount: Option[Int],
    usedPhaseCount: Option[Int],
    associatedStationCount: Option[Int],
    usedStationCount: Option[Int],
    depthPhaseCount: Option[Int],
    standardError: Option[Double],
    azimuthalGap: Option[Double],
    secondaryAzimuthalGap: Option[Double],
    groundTruthLevel: Option[String],
    minimumDistance: Option[Double],
    maximumDistance: Option[Double],
    medianDistance: Option[Double]
)

case class RealQuantity(
    value: Double,
    uncertainty: Option[Double]
)

case class ResourceReference(
    resourceId: String
)

case class TimeQuantity(
    value: ZonedDateTime,
    uncertainty: Option[Double]
)

object CreationInfo:
  given Codec[CreationInfo] = deriveCodec

object Comment:
  given Codec[Comment] = deriveCodec

object ResourceReference:
  given Codec[ResourceReference] = deriveCodec

object EventDescription:
  given Codec[EventDescription] = deriveCodec

object TimeQuantity:
  given Codec[TimeQuantity] = deriveCodec

object RealQuantity:
  given Codec[RealQuantity] = deriveCodec

object IntegerQuantity:
  given Codec[IntegerQuantity] = deriveCodec

object OriginQuality:
  given Codec[OriginQuality] = deriveCodec

object CompositeTime:
  given Codec[CompositeTime] = deriveCodec

object Origin:
  given Codec[Origin] = deriveCodec

object Magnitude:
  given Codec[Magnitude] = deriveCodec

object Event:
  given Codec[Event] = deriveCodec
