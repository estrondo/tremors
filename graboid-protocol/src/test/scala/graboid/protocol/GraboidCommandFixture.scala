package graboid.protocol

import tremors.generator.KeyGenerator
import tremors.generator.KeyLength

object GraboidCommandFixture:

  def createDataCentre(): CreateDataCentre = CreateDataCentre(
    commandId = KeyGenerator.generate(KeyLength.Short),
    id = KeyGenerator.generate(KeyLength.Long),
    event = Some(s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/event/1"),
    dataselect = Some(s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/dataselect/1")
  )

  def updateDataCentre(): UpdateDataCentre = UpdateDataCentre(
    commandId = KeyGenerator.generate(KeyLength.Short),
    KeyGenerator.generate(KeyLength.Long),
    event = Some(s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/event/1"),
    dataselect = Some(s"http://${KeyGenerator.generate(KeyLength.Long)}/fdsn/dataselect/1")
  )

  def deleteDataCentre(): DeleteDataCentre = DeleteDataCentre(
    commandId = KeyGenerator.generate(KeyLength.Short),
    id = KeyGenerator.generate(KeyLength.Long)
  )
