package graboid.quakeml.reader

import graboid.GraboidException
import graboid.quakeml.parser.Element
import java.time.ZonedDateTime
import tremors.quakeml.Comment
import tremors.quakeml.CompositeTime
import tremors.quakeml.CreationInfo
import tremors.quakeml.Event
import tremors.quakeml.EventDescription
import tremors.quakeml.IntegerQuantity
import tremors.quakeml.Magnitude
import tremors.quakeml.Origin
import tremors.quakeml.OriginQuality
import tremors.quakeml.RealQuantity
import tremors.quakeml.ResourceReference
import tremors.quakeml.TimeQuantity

trait ElementReader[+T]:

  def apply(element: Element): T

object ElementReader:

  given ElementReader[Event] with
    override def apply(element: Element): Event =
      Event(
        publicId = readAttribute("publicID", element),
        preferredOriginId = readChild("preferredOriginID", element),
        preferredMagnitudeId = readChild("preferredMagnitudeID", element),
        preferredFocalMechanismId = readChild("preferredFocalMechanismID", element),
        `type` = readChild("type", element),
        typeUncertainty = readChild("typeUncertainty", element),
        description = readChild("description", element),
        comment = readChild("comment", element),
        creationInfo = readChild("creationInfo", element),
        origin = readChild("origin", element),
        magnitude = readChild("magnitude", element),
      )

    given ElementReader[ResourceReference] with

      override def apply(element: Element): ResourceReference =
        element.content match
          case Some(text) => ResourceReference(text)
          case None       => throw GraboidException.QuakeML("ResourceReference: Unexpected empty element!")

    given [T: TextReader]: ElementReader[T] with

      override def apply(element: Element): T =
        element.content match
          case Some(text) => summon[TextReader[T]](text)
          case None       => throw GraboidException.QuakeML("String: Unexpected empty element!")

    given ElementReader[EventDescription] with

      override def apply(element: Element): EventDescription =
        EventDescription(
          text = readChild("text", element),
          `type` = readChild("type", element),
        )

    given ElementReader[Comment] with

      override def apply(element: Element): Comment =
        Comment(
          text = readChild("text", element),
          id = readChild("id", element),
          creationInfo = readChild("creationInfo", element),
        )

    given ElementReader[CreationInfo] with

      override def apply(element: Element): CreationInfo =
        CreationInfo(
          agencyId = readChild("agencyID", element),
          agencyUri = readChild("agencyURI", element),
          author = readChild("author", element),
          authorUri = readChild("authorURI", element),
          creationTime = readChild("creationTime", element),
          version = readChild("version", element),
        )

    given ElementReader[Origin] with

      override def apply(element: Element): Origin =
        Origin(
          publicId = readAttribute("publicID", element),
          time = readChild("time", element),
          longitude = readChild("longitude", element),
          latitude = readChild("latitude", element),
          depth = readChild("depth", element),
          depthType = readChild("depthType", element),
          timeFixed = readChild("timeFixed", element),
          epicenterFixed = readChild("epicenterFixed", element),
          referenceSystemId = readChild("referenceSystemID", element),
          methodId = readChild("methodId", element),
          earthModelId = readChild("earthModelID", element),
          compositeTime = readChild("compositeTime", element),
          quality = readChild("quality", element),
          `type` = readChild("type", element),
          region = readChild("region", element),
          evaluationMode = readChild("evaluationMode", element),
          evaluationStatus = readChild("evaluationStatus", element),
          comment = readChild("comment", element),
          creationInfo = readChild("creationInfo", element),
        )

    given ElementReader[Magnitude] with

      override def apply(element: Element): Magnitude =
        Magnitude(
          publicId = readAttribute("publicID", element),
          mag = readChild("mag", element),
          `type` = readChild("type", element),
          originId = readChild("originID", element),
          methodId = readChild("methodID", element),
          stationCount = readChild("stationCount", element),
          azimuthalGap = readChild("azimuthalGap", element),
          evaluationMode = readChild("evaluationMode", element),
          evaluationStatus = readChild("evaluationStatus", element),
          comment = readChild("comment", element),
          creationInfo = readChild("creationInfo", element),
        )

    given ElementReader[ZonedDateTime] with

      override def apply(element: Element): ZonedDateTime =
        element.content match
          case Some(text) => ZonedDateTime.parse(text)
          case None       => throw GraboidException.QuakeML("ZonedDateTime: Unexpected empty element!")

    given ElementReader[TimeQuantity] with

      override def apply(element: Element): TimeQuantity =
        TimeQuantity(
          value = readChild("value", element),
          uncertainty = readChild("uncertainty", element),
        )

    given ElementReader[RealQuantity] with

      override def apply(element: Element): RealQuantity =
        RealQuantity(
          value = readChild("value", element),
          uncertainty = readChild("uncertainty", element),
        )

    given ElementReader[IntegerQuantity] with

      override def apply(element: Element): IntegerQuantity =
        IntegerQuantity(
          value = readChild("value", element),
          uncertainty = readChild("uncertainty", element),
        )

    given ElementReader[CompositeTime] with

      override def apply(element: Element): CompositeTime =
        CompositeTime(
          year = readChild("year", element),
          month = readChild("month", element),
          day = readChild("day", element),
          hour = readChild("hour", element),
          minute = readChild("minute", element),
          second = readChild("second", element),
        )

    given ElementReader[OriginQuality] with

      override def apply(element: Element): OriginQuality =
        OriginQuality(
          associatedPhaseCount = readChild("associatedPhaseCount", element),
          usedPhaseCount = readChild("usedPhaseCount", element),
          associatedStationCount = readChild("associatedStationCount", element),
          usedStationCount = readChild("usedStationCount", element),
          depthPhaseCount = readChild("depthPhaseCount", element),
          standardError = readChild("standardError", element),
          azimuthalGap = readChild("azimuthalGap", element),
          secondaryAzimuthalGap = readChild("secondaryAzimuthalGap", element),
          groundTruthLevel = readChild("groundTruthLevel", element),
          minimumDistance = readChild("minimumDistance", element),
          maximumDistance = readChild("maximumDistance", element),
          medianDistance = readChild("medianDistance", element),
        )
