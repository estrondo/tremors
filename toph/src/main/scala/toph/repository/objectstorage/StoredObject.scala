package toph.repository.objectstorage

import io.github.arainko.ducktape.Field
import java.time.ZonedDateTime
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer
import toph.model.objectstorage.ObjectContent

case class StoredObject(
    _key: String,
    owner: String,
    name: String,
    folder: String,
    path: String,
    contentType: String,
    content: Array[Byte],
    createdAt: ZonedDateTime,
    updatedAt: ZonedDateTime,
)

object StoredObject:

  given FarangoTransformer[CreateObject, StoredObject] = DucktapeTransformer(
    Field.computed(_.path, x => s"${x.owner}/${x.folder}/${x.name}"),
    Field.renamed(_._key, _.key),
    Field.renamed(_.createdAt, _.now),
    Field.renamed(_.updatedAt, _.now),
  )

  given FarangoTransformer[StoredObject, K] = DucktapeTransformer()

  given FarangoTransformer[StoredObject, ObjectContent] = DucktapeTransformer(
    Field.renamed(_.key, _._key),
  )
