package toph.model.objectstorage

case class ObjectPath(
    owner: String,
    folder: String,
    name: String,
):

  def canonicalPath = s"$owner/$folder/$name"
