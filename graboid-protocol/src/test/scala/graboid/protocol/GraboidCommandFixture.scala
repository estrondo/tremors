package graboid.protocol

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object GraboidCommandFixture:

  def createDataCentre(): CreateDataCentre = CreateDataCentre(
    commandId = KeyGenerator.generate(KeyLength.Short),
    id = KeyGenerator.generate(KeyLength.Long),
    url = s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/query"
  )

  def updateDataCentre(): UpdateDataCentre = UpdateDataCentre(
    commandId = KeyGenerator.generate(KeyLength.Short),
    KeyGenerator.generate(KeyLength.Long),
    url = s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/query"
  )

  def deleteDataCentre(): DeleteDataCentre = DeleteDataCentre(
    commandId = KeyGenerator.generate(KeyLength.Short),
    id = KeyGenerator.generate(KeyLength.Long)
  )
