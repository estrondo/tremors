package testkit.quakeml

import quakeml.*
import testkit.core.createZonedDateTime

import java.time.ZonedDateTime

object CreationInfoFixture:

  def createRandom() = CreationInfo(
    agencyID = Some(createRandomString()),
    agencyURI = Some(ResourceReferenceFixture.createRandom()),
    author = Some(s"Author-${createRandomString()}"),
    authorURI = Some(ResourceReferenceFixture.createRandom()),
    creationTime = Some(createZonedDateTime()),
    version = Some("1.0")
  )
