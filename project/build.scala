import sbt._
import Keys._

//import spray.revolver.RevolverPlugin._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.scalamacros",
    version := "0.0.1",
    scalaVersion := "2.10.1",
    scalacOptions ++= Seq(),
    libraryDependencies += "javax.ws.rs" % "jsr311-api" % "1.1.1"
  )
}

object MyBuild extends Build {
  import BuildSettings._
  import com.github.siasia.WebPlugin.webSettings

  lazy val macros: Project = Project(
    "jax-rs_macros",
    file("jax-rs_macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)
    ) ++ Seq (
      libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1",
      libraryDependencies += "org.scalatra.rl" %% "rl" % "0.4.2"
    )
  )
  
  lazy val test: Project = Project(
    "test",
    file("test"),
    //settings = buildSettings ++ Revolver.settings ++ Seq(
    settings = buildSettings ++ webSettings ++ Seq(
      //libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.0.2.v20130417",
      //libraryDependencies += "org.eclipse.jetty" % "jetty-servlet" % "9.0.2.v20130417",
      libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "8.1.10.v20130312" % "container"
    )
  ) dependsOn(macros)
}