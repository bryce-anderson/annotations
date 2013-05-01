package jaxmacros

import org.specs2.mutable.Specification

import javax.ws.rs._

/**
 * @author Bryce Anderson
 *         Created on 5/1/13
 */
class MaxMacrosSpec extends Specification {


  "Macros" should {
    "Build a Route without params" in {
      class TestClass {
        @GET
        def one() = { println("One"); "one" }
      }
      val routes = jaxmacros.TestBuilder.buildClass[TestClass]
      val minimal = MinimalContext("nowhere", jaxed.Get)
      routes.get("one").map(f => f(minimal)) must_== Some("one")
    }

    "Handle route params" in {
      class Test {
        @GET
        def one(in: Int) = in.toString
      }
      val minimal = MinimalContext("nowhere", jaxed.Get, routeParams = Map("in" -> "1"))
      val routes = jaxmacros.TestBuilder.buildClass[Test]
      routes.get("one").map(f => f(minimal)) must_== Some("1")
    }

    "Handle query params" in {
      class Test {
        @GET
        def one(@QueryParam("in") in: Int) = in.toString
      }
      val minimal = MinimalContext("nowhere", jaxed.Get, queryParams = Map("in" -> "1"))
      val routes = jaxmacros.TestBuilder.buildClass[Test]
      routes.get("one").map(f => f(minimal)) must_== Some("1")
    }

    "Handle form params" in {
      class Test {
        @GET
        def one(@FormParam("in") in: Int) = in.toString
      }
      val minimal = MinimalContext("nowhere", jaxed.Get, formParams = Map("in" -> "1"))
      val routes = jaxmacros.TestBuilder.buildClass[Test]
      routes.get("one").map(f => f(minimal)) must_== Some("1")
    }
  }
}
