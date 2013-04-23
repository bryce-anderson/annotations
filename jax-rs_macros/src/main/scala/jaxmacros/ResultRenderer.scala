package jaxmacros

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
 * @author Bryce Anderson
 *         Created on 4/23/13 at 8:15 AM
 */

/*
Trait that takes responsibility for rendering the result of a route. To add new formats, stack
more traits and use a super call to pass results down the chain.
 */
trait ResultRenderer {
//  def renderResponse(req: HttpServletRequest, resp: HttpServletResponse, result: Any) = result match {
//    case Unit => Unit
//    case s: String => resp.getWriter().write(s)
//    case xml: xml.NodeSeq => resp.getWriter().write(xml.toString())
//    case e => throw new java.util.UnknownFormatConversionException(s"Cannot render result of type '${e.getClass}")
//  }
  def renderResponse(req: HttpServletRequest, resp: HttpServletResponse, result: Any)  = {}
}
