package jaxmacros

import org.specs2.mutable.Specification

import javax.ws.rs._

/**
 * @author Bryce Anderson
 *         Created on 5/1/13
 */
class MaxMacrosSpec extends Specification {

  class TestClass {
    @GET
    def one() = {
      println("One")
      "one"
    }
  }

  "Macros" should {
    "Build a Route without params" in {
      val routes = jaxmacros.TestBuilder.buildClass[TestClass]
      val minimal = MinimalContext("nowhere", jaxed.Get)
      routes.get("one").map(f => f(minimal)) must_== Some("one")
    }
  }
}
