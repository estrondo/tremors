package toph.repository.objectstorage

import scala.util.Random
import toph.createNewZonedDateTime
import toph.createRandomString

object UpdateObjectFixture {
  def createRandom(): UpdateObject = {
    val random      = new Random()
    val key         = createRandomString(random, 10)
    val contentType = createRandomString(random, 8)
    val content     = Array.fill(16)(random.nextInt(128).toByte)
    val now         = createNewZonedDateTime()
    UpdateObject(key, contentType, content, now)
  }
}

object CreateObjectFixture {
  def createRandom(): CreateObject = {
    val random      = new Random()
    val key         = createRandomString(random, 10)
    val owner       = createRandomString(random, 8)
    val folder      = createRandomString(random, 6)
    val name        = createRandomString(random, 12)
    val contentType = createRandomString(random, 8)
    val content     = Array.fill(16)(random.nextInt(128).toByte)
    val now         = createNewZonedDateTime()
    CreateObject(key, owner, folder, name, contentType, content, now)
  }
}
