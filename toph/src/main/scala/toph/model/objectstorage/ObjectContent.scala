package toph.model.objectstorage

case class ObjectContent(
    key: String,
    name: String,
    folder: String,
    contentType: String,
    content: Array[Byte],
)
