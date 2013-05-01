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
    def one() = "one"
  }

  "Macros" should {
    "Hello" in {}
  }
}
