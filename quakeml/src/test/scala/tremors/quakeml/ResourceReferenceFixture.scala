package tremors.quakeml

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object ResourceReferenceFixture:

  def createRandom(): ResourceReference = ResourceReference(
    resourceId = KeyGenerator.generate(KeyLength.Medium),
  )
