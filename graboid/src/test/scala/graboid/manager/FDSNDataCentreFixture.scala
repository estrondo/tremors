package graboid.manager

import graboid.FDSNDataCentre
import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object FDSNDataCentreFixture {

  def createRandom(): FDSNDataCentre = FDSNDataCentre(
    id = KeyGenerator.generate(KeyLength.Short),
    url = s"http://${KeyGenerator.generate(KeyLength.Medium)}/fdsn"
  )
}
