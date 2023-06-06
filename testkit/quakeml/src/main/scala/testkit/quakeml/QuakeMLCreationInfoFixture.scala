package testkit.quakeml

import quakeml.QuakeMLCreationInfo
import testkit.core.createZonedDateTime
object QuakeMLCreationInfoFixture:

  def createRandom() = QuakeMLCreationInfo(
    agencyID = Some(createRandomString()),
    agencyURI = Some(QuakeMLResourceReferenceFixture.createRandom()),
    author = Some(s"Author-${createRandomString()}"),
    authorURI = Some(QuakeMLResourceReferenceFixture.createRandom()),
    creationTime = Some(createZonedDateTime()),
    version = Some("1.0")
  )
