package toph.model.objectstorage

import scala.util.Random
import toph.createRandomString

object FetchObjectOperationFixture:
  def createRandom(): FetchObjectOperation =
    val random = new Random()
    val id     = createRandomString(random, 10)
    val name   = createRandomString(random, 8)
    val folder = createRandomString(random, 6)
    FetchObjectOperation(id, name, folder)

object FetchFolderOperationFixture:
  def createRandom(): FetchFolderOperation =
    val random = new Random()
    val id     = createRandomString(random, 10)
    val name   = createRandomString(random, 8)
    FetchFolderOperation(id, name)
