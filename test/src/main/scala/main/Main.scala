package main

import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import jaxmacros.AnnotationHandler

object Main {
  def main(args: Array[String]) {
    println("Hello world")
    val annHandler = AnnotationHandler
      .mapClass[TestClass]("foo/:bar/cats")
      .mapClass[TestClass2]("testclass2/:bar/cats")

    val server = new Server(8080)
    server.setHandler(annHandler)

    server.start()
    // server.stop()
    server.join()
  }
}
