import sbt._
import sbt.Keys._

object ITPlugin extends AutoPlugin {

  lazy val IT = config("it") extend (Test)

  override def projectConfigurations: Seq[Configuration] = Seq(IT)

  override def projectSettings: Seq[Setting[_]] = inConfig(IT)(Defaults.testSettings) ++ Seq(
    IT / parallelExecution := false
  )
}
