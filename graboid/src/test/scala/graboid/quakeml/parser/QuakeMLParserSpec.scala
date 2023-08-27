package graboid.quakeml.parser

import graboid.GraboidException
import graboid.GraboidSpec
import java.time.ZonedDateTime
import tremors.quakeml.CreationInfo
import tremors.quakeml.Event
import tremors.quakeml.EventDescription
import tremors.quakeml.Magnitude
import tremors.quakeml.Origin
import tremors.quakeml.OriginQuality
import tremors.quakeml.RealQuantity
import tremors.quakeml.ResourceReference
import tremors.quakeml.TimeQuantity
import zio.test.Assertion
import zio.test.assert
import zio.test.assertTrue

object QuakeMLParserSpec extends GraboidSpec:

  def spec = suite("QuakeMLParserSpec")(
    suite("When it's reading a valid QuakeML stream...")(
      test("It should read all events on it.") {
        for events <- QuakeMLParser(readFile("test-data/usp-quakeml.xml")).runCollect
        yield assertTrue(
          events == List(
            Event(
              publicId = ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/usp2023coos"),
              preferredOriginId = Some(
                ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/Origin/20230206115302.825148.262559")
              ),
              preferredMagnitudeId = Some(
                ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/Magnitude/20230206115320.373743.262669")
              ),
              preferredFocalMechanismId = None,
              `type` = Some("earthquake"),
              typeUncertainty = None,
              description = Vector(
                EventDescription(
                  text = "Turkey",
                  `type` = Some("region name")
                )
              ),
              comment = Nil,
              creationInfo = Some(
                CreationInfo(
                  agencyId = Some("USP"),
                  agencyUri = None,
                  author = Some("scevent@seisMaster"),
                  authorUri = None,
                  creationTime = Some(ZonedDateTime.parse("2023-02-06T10:35:48.435792Z")),
                  version = None
                )
              ),
              origin = Vector(
                Origin(
                  publicId =
                    ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/Origin/20230206115302.825148.262559"),
                  time = TimeQuantity(
                    value = ZonedDateTime.parse("2023-02-06T10:24:49.141081Z"),
                    uncertainty = Some(0.1746616513)
                  ),
                  longitude = RealQuantity(
                    value = 37.19106674,
                    uncertainty = Some(3.555669069)
                  ),
                  latitude = RealQuantity(
                    value = 37.96024323,
                    uncertainty = Some(5.055521488)
                  ),
                  depth = Some(
                    RealQuantity(
                      value = 10000.0,
                      uncertainty = Some(0.0)
                    )
                  ),
                  depthType = Some("operator assigned"),
                  timeFixed = None,
                  epicenterFixed = None,
                  referenceSystemId = None,
                  methodId = None,
                  earthModelId = Some(ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/iasp91")),
                  compositeTime = None,
                  quality = Some(
                    OriginQuality(
                      associatedPhaseCount = Some(35),
                      usedPhaseCount = Some(35),
                      associatedStationCount = Some(35),
                      usedStationCount = Some(35),
                      depthPhaseCount = Some(0),
                      standardError = Some(0.4554391151),
                      azimuthalGap = Some(89.08201599),
                      secondaryAzimuthalGap = None,
                      groundTruthLevel = None,
                      minimumDistance = Some(3.923534632),
                      maximumDistance = Some(128.2778168),
                      medianDistance = Some(83.76132202)
                    )
                  ),
                  `type` = None,
                  region = None,
                  evaluationMode = Some("manual"),
                  evaluationStatus = Some("confirmed"),
                  comment = Nil,
                  creationInfo = Some(
                    CreationInfo(
                      agencyId = Some("USP"),
                      agencyUri = None,
                      author = Some("jroberto"),
                      authorUri = None,
                      creationTime = Some(ZonedDateTime.parse("2023-02-06T11:53:02.826141Z")),
                      version = None
                    )
                  )
                )
              ),
              magnitude = Vector(
                Magnitude(
                  publicId = ResourceReference(resourceId =
                    "smi:org.gfz-potsdam.de/geofon/Magnitude/20230206115320.373743.262669"
                  ),
                  mag = RealQuantity(
                    value = 7.500440426,
                    uncertainty = Some(0.08662283642)
                  ),
                  `type` = Some("mB"),
                  originId = Some(
                    ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/Origin/20230206115302.825148.262559")
                  ),
                  methodId = Some(ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/trimmed_mean")),
                  stationCount = Some(14),
                  azimuthalGap = None,
                  evaluationMode = None,
                  evaluationStatus = Some("confirmed"),
                  comment = Nil,
                  creationInfo = Some(
                    CreationInfo(
                      agencyId = Some("USP"),
                      agencyUri = None,
                      author = Some("jroberto"),
                      authorUri = None,
                      creationTime = Some(ZonedDateTime.parse("2023-02-06T11:53:20.373779Z")),
                      version = None
                    )
                  )
                )
              )
            ),
            Event(
              publicId = ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/usp2023cnwr"),
              preferredOriginId = Some(
                ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/Origin/20230206095253.697644.22236")
              ),
              preferredMagnitudeId = Some(
                ResourceReference(
                  resourceId = "smi:org.gfz-potsdam.de/geofon/Magnitude/20230206095320.904994.22398"
                )
              ),
              preferredFocalMechanismId = None,
              `type` = Some("earthquake"),
              typeUncertainty = None,
              description = Vector(
                EventDescription(
                  text = "Turkey",
                  `type` = Some("region name")
                )
              ),
              comment = Nil,
              creationInfo = Some(
                CreationInfo(
                  agencyId = Some("USP"),
                  agencyUri = None,
                  author = Some("scevent@seisMaster"),
                  authorUri = None,
                  creationTime = Some(ZonedDateTime.parse("2023-02-06T01:28:14.857454Z")),
                  version = None
                )
              ),
              origin = Vector(
                Origin(
                  publicId =
                    ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/Origin/20230206095253.697644.22236"),
                  time = TimeQuantity(
                    value = ZonedDateTime.parse("2023-02-06T01:17:34.802569Z"),
                    uncertainty = Some(0.1647570644)
                  ),
                  longitude = RealQuantity(
                    value = 37.02080917,
                    uncertainty = Some(3.65321498)
                  ),
                  latitude = RealQuantity(
                    value = 37.12042618,
                    uncertainty = Some(4.895498364)
                  ),
                  depth = Some(
                    RealQuantity(
                      value = 18000.0,
                      uncertainty = Some(0.0)
                    )
                  ),
                  depthType = Some("operator assigned"),
                  timeFixed = None,
                  epicenterFixed = None,
                  referenceSystemId = None,
                  methodId = None,
                  earthModelId = Some(ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/iasp91")),
                  compositeTime = None,
                  quality = Some(
                    OriginQuality(
                      associatedPhaseCount = Some(45),
                      usedPhaseCount = Some(45),
                      associatedStationCount = Some(45),
                      usedStationCount = Some(45),
                      depthPhaseCount = Some(0),
                      standardError = Some(0.2862978256),
                      azimuthalGap = Some(90.90810394),
                      secondaryAzimuthalGap = None,
                      groundTruthLevel = None,
                      minimumDistance = Some(4.303744793),
                      maximumDistance = Some(147.1859741),
                      medianDistance = Some(86.68536377)
                    )
                  ),
                  `type` = None,
                  region = None,
                  evaluationMode = Some("manual"),
                  evaluationStatus = Some("confirmed"),
                  comment = Nil,
                  creationInfo = Some(
                    CreationInfo(
                      agencyId = Some("USP"),
                      agencyUri = None,
                      author = Some("cleusa"),
                      authorUri = None,
                      creationTime = Some(ZonedDateTime.parse("2023-02-06T09:52:53.698059Z")),
                      version = None
                    )
                  )
                )
              ),
              magnitude = Vector(
                Magnitude(
                  publicId = ResourceReference(resourceId =
                    "smi:org.gfz-potsdam.de/geofon/Magnitude/20230206095320.904994.22398"
                  ),
                  mag = RealQuantity(
                    value = 7.781266634,
                    uncertainty = Some(0.0808178231)
                  ),
                  `type` = Some("mB"),
                  originId = Some(
                    ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/Origin/20230206095253.697644.22236")
                  ),
                  methodId = Some(ResourceReference(resourceId = "smi:org.gfz-potsdam.de/geofon/mean")),
                  stationCount = Some(2),
                  azimuthalGap = None,
                  evaluationMode = None,
                  evaluationStatus = Some("confirmed"),
                  comment = Nil,
                  creationInfo = Some(
                    CreationInfo(
                      agencyId = Some("USP"),
                      agencyUri = None,
                      author = Some("cleusa"),
                      authorUri = None,
                      creationTime = Some(ZonedDateTime.parse("2023-02-06T09:53:20.905020Z")),
                      version = None
                    )
                  )
                )
              )
            )
          )
        )
      }
    ),
    suite("When it's reading a invalid QuakeML stream...")(
      test("It should fail.") {
        for exit <- QuakeMLParser(readFile("test-data/event-without-id-usp-quakeml.xml")).runCollect.exit
        yield assert(exit)(Assertion.failsWithA[GraboidException.QuakeMLException])
      }
    )
  )
