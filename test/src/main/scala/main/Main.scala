package main

import org.eclipse.jetty.server._
import jaxmacros.{RouteNode, AnnotationHandler}
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}

object Main {
  def main(args: Array[String]) {
    println("Hello world")
    val rootNode = RouteNode()
     .mapClass[TestClass]("/foo/:bar/cats")
     .mapClass[TestClass2]("/testclass2/:bar/cats")
     .mapClass[TestClass3]("/testclass3/:bar")
     .mapClass[WithConstrutor]("")

    val subRoute = new RouteNode("jimbo") with DoubleRenderer
    subRoute.mapClass[DoubleClass]("/double")

    rootNode.addRoute(subRoute)


    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.addServlet(new ServletHolder(new AnnotationHandler(rootNode)), "/*")

    val server = new Server(8080)
    server.setHandler(context)

    server.start()
    // server.stop()
    server.join()
  }
}
