package jaxed.scalatra

import jaxed.scalatramacros.ScalatraJaxSupport
import javax.ws.rs.GET

/**
 * @author Bryce Anderson
 *         Created on 5/4/13
 */

class Test {
  @GET
  def doGet() = <html><body>Hello world from Test</body></html>
}

class Main extends ScalatraJaxSupport {

  bindClass[Test]("/helloworld")

  get("/hello") { "Hello standard GET" }
}
