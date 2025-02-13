package toph.model

import scala.util.Random

object ProtoAccountFixture:

  def createRandom(): ProtoAccount = ProtoAccount(
    name = Some(s"Gagarin ${Random.nextInt(10)}"),
  )
