import Dependencies._

version := "0.1"

scalaVersion := "2.13.8"

name := "ProductOfferAggregator"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation:false" /*FIXME*/ ,
  "-feature",
  "-Xasync",
  "-Xfatal-warnings",
  "-language:higherKinds",
  "-Ybackend-parallelism", java.lang.Runtime.getRuntime.availableProcessors.toString
)

val commonSettings = Seq(
  organization := "jskrzypczynski",
  libraryDependencies ++= Seq(log4j) ++ circe ++ cats ++ fs2 ++ doobie ++ http4s ++ pureconfig,
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"))


lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "ProductOfferAggregator",
    assembly / mainClass := Some("com.jskrzypczynski.poa.main.Main")
  )
