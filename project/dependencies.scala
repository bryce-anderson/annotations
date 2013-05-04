import sbt._
import Keys._

object Dependencies {
  lazy val servlet = "javax.servlet" % "javax.servlet-api" % "3.0.1"
  lazy val rl = "org.scalatra.rl" %% "rl" % "0.4.2"
  lazy val Specs2 = "org.specs2" %% "specs2" % "1.13"
  
  // lazy val jettyContainer = "org.eclipse.jetty" % "jetty-servlet" % "9.0.2.v20130417" // Doesn't work with webplugin
  lazy val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % "8.1.10.v20130312" % "container"
  
  // lazy val scalatra = "org.scalatra" % "scalatra" % "2.2.0"
  lazy val scalatraBranch = "2.2.x_2.10"
  
  lazy val scalatraGitProject = ProjectRef(uri("https://github.com/scalatra/scalatra.git#%s".format(scalatraBranch)), "scalatra")
            
  
}