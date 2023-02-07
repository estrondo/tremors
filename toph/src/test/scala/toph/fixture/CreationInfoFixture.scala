package toph.fixture

import core.KeyGenerator
import testkit.core.createZonedDateTime
import toph.model.CreationInfo

object CreationInfoFixture:

  def createRandom() = CreationInfo(
    agencyID = Some(KeyGenerator.next8()),
    agencyURI = Some(KeyGenerator.next8()),
    author = Some(KeyGenerator.next8()),
    creationTime = Some(createZonedDateTime()),
    version = Some(KeyGenerator.next8())
  )
