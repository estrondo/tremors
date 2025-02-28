package toph.model.objectstorage

import toph.createRandomString

import scala.util.Random

object UpdateObjectOperationFixture {
  def createRandom(): UpdateObjectOperation = {
    val random              = new Random()
    val id                  = createRandomString(random, 10)
    val name                = createRandomString(random, 8)
    val folder              = createRandomString(random, 6)
    val shouldReturnContent = random.nextBoolean()
    val contentType         = createRandomString(random, 8)
    val content             = Array.fill(16)(random.nextInt(128).toByte)
    UpdateObjectOperation(id, name, folder, shouldReturnContent, contentType, content)
  }
}

object RemoveObjectOperationFixture {
  def createRandom(): RemoveObjectOperation = {
    val random              = new Random()
    val id                  = createRandomString(random, 10)
    val folder              = createRandomString(random, 6)
    val name                = createRandomString(random, 8)
    val shouldReturnContent = random.nextBoolean()
    RemoveObjectOperation(id, folder, name, shouldReturnContent)
  }
}

object RemoveFolderOperationFixture {
  def createRandom(): RemoveFolderOperation = {
    val random              = new Random()
    val id                  = createRandomString(random, 10)
    val name                = createRandomString(random, 8)
    val shouldReturnContent = random.nextBoolean()
    RemoveFolderOperation(id, name, shouldReturnContent)
  }
}
