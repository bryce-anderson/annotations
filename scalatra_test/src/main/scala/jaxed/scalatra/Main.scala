package jaxed.scalatra

import jaxed.scalatramacros.ScalatraJaxSupport
import javax.ws.rs.GET
import javax.servlet.http.HttpServletResponse

/**
 * @author Bryce Anderson
 *         Created on 5/4/13
 */

class Test {
  @GET
  def doGet() = <html><body>Hello world from Test </body></html>
}

class Test2 {
  @GET
  def doGet(resp: HttpServletResponse) = {
    resp.getWriter.write("Raw Hello</br>")
    <p>This is not raw.</p>
  }
}

class WithParams {
  @GET
  def withParams(name: String) = s"Hello $name"
}

class Main extends ScalatraJaxSupport {

  bindClass[Test]("/helloworld")
  bindClass[Test2]("/helloworld2")
  bindClass[WithParams]("/hello/:name")

}
