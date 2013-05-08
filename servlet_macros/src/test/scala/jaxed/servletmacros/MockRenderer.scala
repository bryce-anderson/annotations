package jaxed
package servletmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import jaxed.servlet.{ResultRenderer, DoneResult}

/**
 * @author Bryce Anderson
 *         Created on 5/2/13
 */
trait MockRenderer extends ResultRenderer {
  override def renderResponse(req: HttpServletRequest, resp: HttpServletResponse, result: Any): Any =
    DoneResult(result)
}
