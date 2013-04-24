package jaxmacros

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

/**
 * @author Bryce Anderson
 *         Created on 4/19/13 at 2:42 PM
 */

/*
  Trait that represents a single route, eg a GET request or a POST request.
  The route will attempt to run, but errors must be handled by the RouteNode executing the route.
  The return value says whether the route was matched and executed successfully. Exceptions should
  signal that the route matched, but there was a problem of some type during execution.
 */
trait Route {
  def handle(path: String, req: HttpServletRequest, resp: HttpServletResponse): Option[Any]
}

object Route {
  def apply(regex: String)(route: (Match, HttpServletRequest, HttpServletResponse) => Any): Route = {
    val routeRegex = regex.r()
    new Route {
      def handle(path: String, req: HttpServletRequest, resp: HttpServletResponse) =
        routeRegex.findFirstMatchIn(path)
          .map(route(_, req, resp))
    }
  }
}