package tremors.quakeml

object CommentFixture:

  def createRandom() = Comment(
    text = createRandomString(),
    id = Some(ResourceReferenceFixture.createRandom()),
    creationInfo = Some(CreationInfoFixture.createRandom())
  )
