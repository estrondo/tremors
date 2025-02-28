package toph.model.objectstorage

case class FolderPath(
    owner: String,
    name: String,
):

  def canonicalPath = s"$owner/$name"
