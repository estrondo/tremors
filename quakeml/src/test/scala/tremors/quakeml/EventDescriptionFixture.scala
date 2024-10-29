package tremors.quakeml

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object EventDescriptionFixture:

  def createRandom(): EventDescription = EventDescription(
    text = KeyGenerator.generate(KeyLength.Long),
    `type` = Some(KeyGenerator.generate(KeyLength.Long)),
  )
