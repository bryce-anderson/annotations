package jaxmacros

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.util.matching.Regex

/**
 * @author Bryce Anderson
 *         Created on 4/19/13 at 2:42 PM
 */

trait Route {
  def handle(req: HttpServletRequest, resp: HttpServletResponse): Boolean
}

object Route {
}