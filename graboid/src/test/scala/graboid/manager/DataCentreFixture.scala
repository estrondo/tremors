package graboid.manager

import graboid.DataCentre
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object DataCentreFixture {

  def createRandom(): DataCentre = DataCentre(
    id = KeyGenerator.generate(KeyLength.Short),
    event = Some(s"http://${KeyGenerator.generate(KeyLength.Medium)}/fdsn/event/1"),
    dataselect = Some(s"http://${KeyGenerator.generate(KeyLength.Medium)}/fdsn/dataselect/1")
  )
}
