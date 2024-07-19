ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .aggregate(core, app)
  .settings(
    name := "S8_FunctProg_Projet",
    addCommandAlias("run", "app/run")
  )

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    Compile / mainClass := Some("Graph_manager"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "dev.zio" %% "zio" % "2.0.12",
      "dev.zio" %% "zio-json" % "0.6.2"
    )
  )

lazy val app = (project in file("app"))
  .dependsOn(core)
  .settings(
    name := "app",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.12"
    )
  )
