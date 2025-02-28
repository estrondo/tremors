package toph.repository.objectstorage

import java.time.ZonedDateTime

sealed trait Action

case class UpdateObject(
    key: String,
    contentType: String,
    content: Array[Byte],
    now: ZonedDateTime,
) extends Action

case class CreateObject(
    key: String,
    owner: String,
    folder: String,
    name: String,
    contentType: String,
    content: Array[Byte],
    now: ZonedDateTime,
) extends Action
