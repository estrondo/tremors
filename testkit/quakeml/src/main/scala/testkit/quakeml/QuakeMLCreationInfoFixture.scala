package testkit.quakeml

import quakeml.QuakeMLCreationInfo
import quakeml.*
import testkit.core.createZonedDateTime

import java.time.ZonedDateTime
object QuakeMLCreationInfoFixture:

  def createRandom() = QuakeMLCreationInfo(
    agencyID = Some(createRandomString()),
    agencyURI = Some(QuakeMLResourceReferenceFixture.createRandom()),
    author = Some(s"Author-${createRandomString()}"),
    authorURI = Some(QuakeMLResourceReferenceFixture.createRandom()),
    creationTime = Some(createZonedDateTime()),
    version = Some("1.0")
  )
