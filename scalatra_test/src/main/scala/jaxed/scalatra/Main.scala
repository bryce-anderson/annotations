package jaxed.scalatra

import jaxed.scalatramacros.ScalatraJaxSupport
import javax.ws.rs.GET

/**
 * @author Bryce Anderson
 *         Created on 5/4/13
 */

class Test {
  @GET
  def doGet() = "Hello world"
}

class Main extends ScalatraJaxSupport {

  println("Hello world!")

  bindClass[Test]("/helloworld")

  get("/hello") { "Hello standard GET" }
}
