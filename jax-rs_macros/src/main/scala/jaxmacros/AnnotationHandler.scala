package jaxmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import scala.collection.mutable.MutableList

import scala.language.experimental.macros
import scala.reflect.macros.Context

/**
 * @author Bryce Anderson
 *         Created on 4/19/13 at 11:40 AM
 */
class AnnotationHandler(rootNode: RouteNode) extends HttpServlet { self =>

  // Virtual method of AbstractHandler
  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    rootNode.handle(req.getPathInfo, Map.empty[String, String], req, resp)
  }
}
