val scala3Version = "3.3.0"
val http4sVersion = "1.0.0-M39"

lazy val root = project
  .in(file("."))
  .settings(
    name := "ztna",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.5.1",
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
    )
  )
