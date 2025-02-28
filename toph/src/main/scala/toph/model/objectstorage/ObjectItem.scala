package toph.model.objectstorage

import java.time.ZonedDateTime

case class ObjectItem(
    key: String,
    folder: String,
    name: String,
    contentType: String,
    createdAt: ZonedDateTime,
    updatedAt: ZonedDateTime,
)
