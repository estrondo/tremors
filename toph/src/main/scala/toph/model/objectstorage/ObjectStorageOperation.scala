package toph.model.objectstorage

sealed trait ObjectStorageOperation

sealed trait ObjectStorageUpdateOperation extends ObjectStorageOperation

case class UpdateObjectOperation(
    id: String,
    name: String,
    folder: String,
    shouldReturnContent: Boolean,
    contentType: String,
    content: Array[Byte],
) extends ObjectStorageUpdateOperation

case class RemoveObjectOperation(
    id: String,
    folder: String,
    name: String,
    shouldReturnContent: Boolean,
) extends ObjectStorageUpdateOperation

case class RemoveFolderOperation(
    id: String,
    name: String,
    shouldReturnContent: Boolean,
) extends ObjectStorageUpdateOperation

case object EmptyUpdateOperation extends ObjectStorageUpdateOperation

sealed trait ObjectStorageReadOperation extends ObjectStorageOperation

case class FetchObjectOperation(
    id: String,
    name: String,
    folder: String,
) extends ObjectStorageReadOperation

case class FetchFolderOperation(
    id: String,
    name: String,
) extends ObjectStorageReadOperation

case object EmptyReadOperation extends ObjectStorageReadOperation
