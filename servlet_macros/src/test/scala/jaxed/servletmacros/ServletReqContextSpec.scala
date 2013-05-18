package jaxed
package servlet

import org.specs2.mutable.Specification
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.ws.rs.GET

/**
 * @author Bryce Anderson
 *         Created on 5/2/13
 */

class ServletReqContextSpec extends Specification {

  class MockNode extends RouteNode {
    def path = ""
    def getParent = new Route {
      def handle(path: ServletReqContext): Option[Any] = throw new NotImplementedError("Method 'handle' should not be called!")
      def url(params: Map[String, String]): Option[String] = Some("http://test.com")
    }
  }

  "Servlet Macros" should {
    val mocContext = new ServletReqContext("/foo/bar", Get, Map("one" -> "two"), null, null)
    "Run a route" in {
      class Test {
        @GET
        def getHello() = "Hello"
      }

       val node = new MockNode()
       node.mapClass[Test]("/foo/bar")
      node.handle(mocContext) must_== Some("Hello")
    }

    "Add a request" in {
      class Test(req: HttpServletRequest) {
        @GET
        def getHello(req: HttpServletRequest) = "Hello"
      }

      val node = new MockNode()
      node.mapClass[Test]("/foo/bar")
      node.handle(mocContext) must_== Some("Hello")
    }

    "Add a response" in {
      class Test(resp: HttpServletResponse) {
        @GET
        def getHello(resp: HttpServletResponse) = "Hello"
      }

      val node = new MockNode()
      val route = node.mapClass[Test]("/foo/bar")
      node.handle(mocContext) must_== Some("Hello")
      route.url() must_== Some("http://test.com/foo/bar")
    }
  }

}
