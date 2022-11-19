import sbt._
import sbt.Keys._
import scala.sys.process._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

object DockerHelperPlugin extends AutoPlugin {

  lazy val writeDockerImageName = taskKey[File]("It writes the Docker Image Name")

  lazy val writeVersionFile = taskKey[File]("It writes the current project version")

  private def write(parent: File, filename: String, content: String): File = {
    val file = new File(parent, filename)
    IO.write(file, content)
    file
  }

  override def projectSettings: Seq[Setting[_]] = Seq(
    writeDockerImageName := {
      val content = (Docker / dockerAlias).value.toString()
      write(target.value, "docker-image-name", content)
    },
    writeVersionFile     := {
      write(target.value, "version", version.value)
    }
  )
}
