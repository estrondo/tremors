package toph.model

import toph.createNewZonedDateTime
import tremors.generator.KeyGenerator
import tremors.random

object TokenFixture:

  def createRandom(): Token = Token(
    key = KeyGenerator.medium(),
    expiration = createNewZonedDateTime(),
    accountKey = KeyGenerator.medium(),
    accountEmail = s"${Array("albert", "galileo").toIndexedSeq.random}@ec.2",
    accessTokenHash = KeyGenerator.long(),
    accessTokenExpiration = createNewZonedDateTime(),
    device = Array("android", "iphone", "web").toIndexedSeq.random,
    origin = Array(None, Some("127.0.0.1")).toIndexedSeq.random,
    createdAt = createNewZonedDateTime(),
  )
