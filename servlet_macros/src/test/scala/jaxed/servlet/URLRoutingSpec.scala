package jaxed.servlet

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
      matcher.reverse(Map("bar" -> "cats")) must_== Some("/foo/cats/cats")
    }

    "Generate the reverse route" in {
      val matcher = m("/foo/:bar/cats")
      matcher.reverse(Map("bar" -> "bazz")) must_== Some("/foo/bazz/cats")
    }

    "Generate the two param reverse route" in {
      val matcher = m("/foo/:bar/cats/:bazz")
      val map = Map("bar" -> "param1", "bazz" -> "param2")
      matcher.reverse(map) must_== Some("/foo/param1/cats/param2")
    }

    "Fail on a reverse route with wrong params" in {
      val matcher = m("/foo/:bar/cats")
      matcher.reverse(Map("foo" -> "bazz")) must_== None
    }

    "Combine two rountes" in {
      val m1 = m("/foo/:bar")
      val m2 = m("/baz/:bah")
      val map = Map("bar" -> "one", "bah" -> "two")

      val m3 = m1 + m2
      m3.regex.toString must_== "^/foo/([^/?#]+)/baz/([^/?#]+)$"
      m3.reverse(map) must_== Some("/foo/one/baz/two")
    }
  }

}
