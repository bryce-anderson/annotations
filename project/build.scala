import sbt._
import Keys._

import Dependencies._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.scalamacros",
    version := "0.0.1",
    scalaVersion := "2.10.1",
    scalacOptions ++= Seq(),
    libraryDependencies += "javax.ws.rs" % "jsr311-api" % "1.1.1"
  )
  
  val macroSettings = Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)
    )
}

object MyBuild extends Build {
  import BuildSettings._
  import com.github.siasia.WebPlugin.webSettings
  
  lazy val base: Project = Project(
    "base",
    file("base"),
    settings = buildSettings ++ Seq (
      libraryDependencies += rl
    )
  )

  lazy val macros: Project = Project(
    "jax-rs_macros",
    file("jax-rs_macros"),
    settings = buildSettings ++ macroSettings ++ Seq (
      //libraryDependencies += rl
      libraryDependencies += Specs2 % "test"
    )
  ) dependsOn(base)
  
  lazy val servletMacros: Project = Project(
    "servlet_macros",
    file("servlet_macros"),
    settings = buildSettings ++ macroSettings ++ Seq (
      libraryDependencies += servlet
    )
  ) dependsOn(macros)
  
  lazy val test: Project = Project(
    "test",
    file("test"),
    //settings = buildSettings ++ Revolver.settings ++ Seq(
    settings = buildSettings ++ webSettings ++ Seq(
      libraryDependencies += jettyContainer
    )
  ) dependsOn(macros)
}