package graboid.manager

import graboid.DataCentre
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object DataCentreFixture {

  def createRandom(): DataCentre = DataCentre(
    id = KeyGenerator.generate(KeyLength.Short),
    url = s"http://${KeyGenerator.generate(KeyLength.Medium)}/fdsn"
  )
}
