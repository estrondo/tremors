package testkit.quakeml

import quakeml.QuakeMLComment
object QuakeMLCommentFixture:

  def createRandom() = QuakeMLComment(
    text = createRandomString(),
    id = Some(QuakeMLResourceReferenceFixture.createRandom()),
    creationInfo = Some(QuakeMLCreationInfoFixture.createRandom())
  )
