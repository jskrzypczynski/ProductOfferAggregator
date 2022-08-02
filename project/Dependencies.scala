import sbt._

object Dependencies {

  val http4s = Seq(
    "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
    "org.http4s" %% "http4s-circe" % Versions.http4s,
    "org.http4s" %% "http4s-dsl" % Versions.http4s)

  val circe = Seq(
    "io.circe" %% "circe-generic" % Versions.circe,
    "io.circe" %% "circe-generic-extras" % Versions.circe,
  )

  val cats = Seq(
    "org.typelevel" %% "log4cats-slf4j" % Versions.log4cats,
    "org.typelevel" %% "cats-effect" % Versions.catsEffect,
    "org.typelevel" %% "cats-core" % Versions.cats
  )

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % Versions.doobie,
    "org.tpolecat" %% "doobie-h2" % Versions.doobie,
  )

  val fs2 = Seq(
    "co.fs2" %% "fs2-io" % "3.2.10",
    "co.fs2" %% "fs2-core" % "3.2.10"
  )

  val pureconfig = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.17.1",
    "com.github.pureconfig" %% "pureconfig-cats" % "0.17.1",
    "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.1"
  )

  val log4j = "org.slf4j" % "slf4j-log4j12" % "1.7.36"

  val scalatest = "org.scalatest" %% "scalatest" % "3.2.12"
}


object Versions {
  val http4s = "1.0.0-M21"
  val circe = "0.13.0"
  val catsEffect = "3.3.11"
  val cats = "2.7.0"
  val log4cats = "2.2.0"
  val doobie = "1.0.0-RC2"
}
