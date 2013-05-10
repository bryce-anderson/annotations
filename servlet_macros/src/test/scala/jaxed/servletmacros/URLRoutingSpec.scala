package jaxed.servletmacros

import org.specs2.mutable.Specification
import jaxed.servlet.SinatraPathPatternParser

/**
 * @author Bryce Anderson
 *         Created on 5/10/13
 */
class URLRoutingSpec extends Specification {

  def m(s: String, b: Boolean = false) = SinatraPathPatternParser(s, b)

  "URL Routing" should {

    "Find static path" in {
      val matcher = m("/foo/bar")
      matcher("/foo/bar") must_== Some(Map.empty[String, String], "")
    }

    "Extract a param name" in {
      val matcher = m("/foo/:bar")
      matcher("/foo/334") must_== Some(Map("bar" -> "334"), "")
    }

    "Extract a param in the middle of a uri" in {
      val matcher = m("/foo/:bar/cats")
      matcher("/foo/hello%20world/cats") must_== Some(Map("bar" -> "hello%20world"), "")
      matcher.reverse(Map("bar" -> "cats")) must_== Some(("/foo/cats/cats", Map.empty[String, String]))
    }

    "Generate the reverse route" in {
      val matcher = m("/foo/:bar/cats")
      matcher.reverse(Map("bar" -> "bazz")) must_== Some(("/foo/bazz/cats", Map.empty[String, String]))
    }
  }

}
