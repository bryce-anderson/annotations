package jaxed
package servletmacros

import org.specs2.mutable.Specification
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.ws.rs.GET

/**
 * @author Bryce Anderson
 *         Created on 5/2/13
 */

class ServletReqContextSpec extends Specification {

  class MockNode extends RouteNode with MockRenderer

  "Servlet Macros" should {
    val mocContext = ServletReqContext("/foo/bar", Get, Map("one" -> "two"), null, null)
    "Run a route" in {
      class Test {
        @GET
        def getHello() = "Hello"
      }

       val node = new MockNode()
       node.mapClass[Test]("foo/bar")
      node.handle(mocContext) must_== Some(DoneResult("Hello"))
    }

    "Add a request" in {
      class Test(req: HttpServletRequest) {
        @GET
        def getHello(req: HttpServletRequest) = "Hello"
      }

      val node = new MockNode()
      node.mapClass[Test]("foo/bar")
      node.handle(mocContext) must_== Some(DoneResult("Hello"))
    }

    "Add a response" in {
      class Test(resp: HttpServletResponse) {
        @GET
        def getHello(resp: HttpServletResponse) = "Hello"
      }

      val node = new MockNode()
      node.mapClass[Test]("foo/bar")
      node.handle(mocContext) must_== Some(DoneResult("Hello"))
    }
  }

}
