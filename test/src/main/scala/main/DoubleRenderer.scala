package main

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author Bryce Anderson
 *         Created on 4/25/13
 */
trait DoubleRenderer extends jaxmacros.ResultRenderer {
  override def renderResponse(req: HttpServletRequest, resp: HttpServletResponse, result: Any) = result match {
    case d: Double => resp.getWriter.write(d.toString)
    case result => super.renderResponse(req, resp, result)
  }
}
