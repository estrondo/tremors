import sbt._

//noinspection TypeAnnotation
object Dependencies {

  private val ZHttpVersion = "1.0.0.0-RC27"
  private val ZKafkaVersion = "2.0.0-M3"
  private val ZIOVersion = "2.0.0"

  val ZHttp = Seq(
    "io.d11" %% "zhttp" % ZHttpVersion,
    "io.d11" %% "zhttp-test" % ZHttpVersion % Test
  )

  val ZKafka = Seq(
    "dev.zio" %% "zio-kafka" % ZKafkaVersion
  )

  val ZIO = Seq(
    "dev.zio" %% "zio" % ZIOVersion,
    "dev.zio" %% "zio-streams" % ZIOVersion
  )
}