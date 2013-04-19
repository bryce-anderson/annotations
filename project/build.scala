import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.scalamacros",
    version := "0.0.1",
    scalaVersion := "2.10.1",
    scalacOptions ++= Seq(),
    libraryDependencies += "javax.ws.rs" % "jsr311-api" % "1.1.1",
    libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.0.2.v20130417"
  )
}

object MyBuild extends Build {
  import BuildSettings._

  lazy val macros: Project = Project(
    "jax-rs_macros",
    file("jax-rs_macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _))
  )
  
  lazy val test: Project = Project(
    "test",
    file("test"),
    settings = buildSettings
  ) dependsOn(macros)
}