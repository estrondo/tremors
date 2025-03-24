package graboid.protocol

import io.bullet.borer.Codec
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveAllCodecs
import java.time.Duration
import java.time.ZonedDateTime

sealed abstract class GraboidCommand:

  def commandId: String

sealed abstract class DataCentreCommand extends GraboidCommand

sealed abstract class CrawlingCommand extends GraboidCommand

case class CreateDataCentre(
    commandId: String,
    id: String,
    eventEndpoint: Option[String],
    dataselectEndpoint: Option[String],
) extends DataCentreCommand

case class UpdateDataCentre(
    commandId: String,
    id: String,
    eventEndpoint: Option[String],
    dataselectEndpoint: Option[String],
) extends DataCentreCommand

case class DeleteDataCentre(
    commandId: String,
    id: String,
) extends DataCentreCommand

case class RunEventCrawling(
    commandId: String,
    starting: ZonedDateTime,
    ending: ZonedDateTime,
    timeWindow: Duration,
    minMagnitude: Option[Double],
    maxMagnitude: Option[Double],
    magnitudeType: Option[String],
    eventType: Option[String],
) extends CrawlingCommand

case class RunDataCentreEventCrawling(
    commandId: String,
    dataCentre: String,
    starting: ZonedDateTime,
    ending: ZonedDateTime,
    timeWindow: Duration,
    minMagnitude: Option[Double],
    maxMagnitude: Option[Double],
    magnitudeType: Option[String],
    eventType: Option[String],
) extends CrawlingCommand

object GraboidCommand:

  given Codec[GraboidCommand] = deriveAllCodecs
