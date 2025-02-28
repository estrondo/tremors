package toph.model.objectstorage

import scala.util.Random
import toph.createRandomString

object ObjectContentFixture:
  def createRandom(): ObjectContent =
    val random      = new Random()
    val key         = createRandomString(random, 10)
    val name        = createRandomString(random, 8)
    val folder      = createRandomString(random, 6)
    val contentType = createRandomString(random, 8)
    val content     = Array.fill(16)(random.nextInt(128).toByte)
    ObjectContent(key, name, folder, contentType, content)
