package main

import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import jaxmacros.AnnotationHandler

object Main {
  def main(args: Array[String]) {
    println("Hello world")
    val annHandler = new AnnotationHandler
    annHandler.bindClass[TestClass]("foo/:bar/:world/cats")

    val server = new Server(8080)
    val a = new AbstractHandler {
      def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {

        baseRequest.setHandled(true)
        response.getWriter.print("Hello world")
      }
    }

    server.setHandler(a)

    //server.start()
    // server.stop()
    //server.join()

  }
}
