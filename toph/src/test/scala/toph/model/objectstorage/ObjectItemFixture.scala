package toph.model.objectstorage

import scala.util.Random
import toph.createNewZonedDateTime
import toph.createRandomString

object ObjectItemFixture:
  def createRandom(): ObjectItem =
    val random      = new Random()
    val key         = createRandomString(random, 10)
    val folder      = createRandomString(random, 6)
    val name        = createRandomString(random, 8)
    val contentType = createRandomString(random, 8)
    val createdAt   = createNewZonedDateTime()
    val updatedAt   = createNewZonedDateTime()
    ObjectItem(key, folder, name, contentType, createdAt, updatedAt)
