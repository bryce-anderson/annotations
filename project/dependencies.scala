import sbt._
import Keys._

object Dependencies {
  lazy val servlet = "javax.servlet" % "javax.servlet-api" % "3.0.1"
  lazy val rl = "org.scalatra.rl" %% "rl" % "0.4.2"
  
  // lazy val jettyContainer = "org.eclipse.jetty" % "jetty-servlet" % "9.0.2.v20130417" // Doesn't work with webplugin
  lazy val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % "8.1.10.v20130312" % "container"
  
}