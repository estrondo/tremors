package tremors.quakeml

import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object CreationInfoFixture:

  def createRandom() = CreationInfo(
    agencyId = Some(KeyGenerator.generate(KeyLength.Short)),
    agencyUri = Some(ResourceReferenceFixture.createRandom()),
    author = Some(KeyGenerator.generate(KeyLength.Medium)),
    authorUri = Some(ResourceReferenceFixture.createRandom()),
    creationTime = Some(ZonedDateTimeFixture.createRandom()),
    version = Some(KeyGenerator.generate(KeyLength.Short))
  )
