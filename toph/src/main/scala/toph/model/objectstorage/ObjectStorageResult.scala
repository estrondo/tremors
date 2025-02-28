package toph.model.objectstorage

import java.time.ZonedDateTime

sealed trait ObjectStorageResult

case class Error(
    id: String,
    code: String,
    message: String,
    causes: Seq[String],
) extends ObjectStorageResult

case class FetchObjectResult(
    id: String,
    name: String,
    folder: String,
    contentType: String,
    exists: Boolean,
    content: Array[Byte],
) extends ObjectStorageResult

case class FetchFolderResult(
    id: String,
    name: String,
    items: Seq[Item],
) extends ObjectStorageResult

case class UpdateObjectResult(
    id: String,
    name: String,
    folder: String,
    contentType: String,
    content: Array[Byte],
) extends ObjectStorageResult

case class RemoveObjectResult(
    id: String,
    name: String,
    folder: String,
    existed: Boolean,
    content: Array[Byte],
) extends ObjectStorageResult

case class RemoveFolderResult(
    id: String,
    name: String,
    items: Seq[Item],
) extends ObjectStorageResult

case class Item(
    name: String,
    folder: String,
    contentType: String,
    createdAt: ZonedDateTime,
    updatedAt: ZonedDateTime,
)
