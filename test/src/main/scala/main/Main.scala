package main

import org.eclipse.jetty.server._
import jaxmacros.{RouteNode, AnnotationHandler}
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}

object Main {
  def main(args: Array[String]) {
    println("Hello world")
    val rootNode = new RouteNode
    rootNode.mapClass[TestClass]("foo/:bar/cats")
    rootNode.mapClass[TestClass2]("testclass2/:bar/cats")


    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.addServlet(new ServletHolder(new AnnotationHandler(rootNode)), "/*")

    val server = new Server(8080)
    server.setHandler(context)

    server.start()
    // server.stop()
    server.join()
  }
}
