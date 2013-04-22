package jaxmacros

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

/**
 * @author Bryce Anderson
 *         Created on 4/19/13 at 2:42 PM
 */

trait Route {
  def handle(path: String, req: HttpServletRequest, resp: HttpServletResponse): Boolean
}

object Route {
  def apply(regex: String)(route: (Match, HttpServletRequest, HttpServletResponse) => Any): Route = {
    val routeRegex = regex.r()
    new Route {
      def handle(path: String, req: HttpServletRequest, resp: HttpServletResponse) = routeRegex.findFirstMatchIn(path) match {
        case None => false
        case Some(matches) => route(matches, req, resp); true
      }
    }
  }
}