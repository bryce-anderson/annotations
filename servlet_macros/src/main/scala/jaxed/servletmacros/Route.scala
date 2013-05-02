package jaxed.servletmacros

import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import jaxed.RequestContext

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
  def handle(path: ServletReqContext): Option[Any]
}

//object Route {
//  def apply(regex: Regex, method: RequestMethod)(route: (RouteParams, HttpServletRequest, HttpServletResponse) => Any): Route = {
//    println(s"DEBUG: method: $method")
//    new Route {
//      def handle(path: RequestContext, req: HttpServletRequest, resp: HttpServletResponse) =
//        if (path.method == method) {
//        regex.findFirstMatchIn(path.path)
//          .map { matches =>
//            route(path.params ++ namedRegexMatchToMap(matches), req, resp)
//          }
//        } else None
//    }
//  }
//
//  def namedRegexMatchToMap(regex: Match) = new RouteParams {
//
//    def +[B1 >: String](kv: (String, B1)): Map[String, B1] = ???
//    def -(key: String): Map[String, String] = ???
//
//    def get(key: String): Option[String] = {
//      try {
//        Some(regex.group(key))
//      } catch {
//        case t: java.util.NoSuchElementException => None
//      }
//    }
//
//    def iterator: Iterator[(String, String)] = regex.groupNames.toIterator.map(key => (key, get(key).get) )
//  }
//}
