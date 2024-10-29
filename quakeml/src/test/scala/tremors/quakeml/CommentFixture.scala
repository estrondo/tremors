package tremors.quakeml

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object CommentFixture:

  def createRandom() = Comment(
    text = KeyGenerator.generate(KeyLength.Long),
    id = Some(ResourceReferenceFixture.createRandom()),
    creationInfo = Some(CreationInfoFixture.createRandom()),
  )
