package jaxmacros

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

/**
 * @author Bryce Anderson
 *         Created on 4/19/13
 */

/*
  Trait that represents a single route, eg a GET request or a POST request.
  The route will attempt to run, but errors must be handled by the RouteNode executing the route.
  The return value says whether the route was matched and executed successfully. Exceptions should
  signal that the route matched, but there was a problem of some type during execution.
 */

trait Route {
  def handle(path: String, routeParams: Map[String, String], req: HttpServletRequest, resp: HttpServletResponse): Option[Any]
}

object Route {
  def apply(regex: Regex)(route: (Map[String, String], HttpServletRequest, HttpServletResponse) => Any): Route = {
    new Route {
      def handle(path: String, routeParams: Map[String, String], req: HttpServletRequest, resp: HttpServletResponse) =
        regex.findFirstMatchIn(path)
          .map { matches =>
            route(routeParams ++ namedRegexMatchToMap(matches), req, resp)
          }
    }
  }

  def namedRegexMatchToMap(regex: Match) = new Map[String, String] {

    def +[B1 >: String](kv: (String, B1)): Map[String, B1] = ???
    def -(key: String): Map[String, String] = ???

    def get(key: String): Option[String] = {
      try {
        Some(regex.group(key))
      } catch {
        case t: java.util.NoSuchElementException => None
      }
    }

    def iterator: Iterator[(String, String)] = regex.groupNames.toIterator.map(key => (key, get(key).get) )
  }
}
