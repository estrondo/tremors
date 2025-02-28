package toph.model.objectstorage

import scala.util.Random

object ObjectStorageContextFixture {
  def createRandom(): Context = {
    val random = new Random()
    val owner  = random.alphanumeric.take(10).mkString
    val user   = random.alphanumeric.take(10).mkString

    Context(owner, user)
  }
}
