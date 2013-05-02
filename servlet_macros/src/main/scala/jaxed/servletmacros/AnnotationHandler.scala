package jaxed
package servletmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

import scala.language.experimental.macros

/**
 * @author Bryce Anderson
 *         Created on 4/19/13
 */
abstract class AnnotationHandler extends HttpServlet { self =>

  def rootNode: RouteNode

  // Virtual method of AbstractHandler
  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    val method = req.getMethod match {
      case "GET" => Get
      case "POST" => Post
      case "PUT" => Put
      case "DELETE" => Delete
    }
    val rawPath = req.getRequestURI().substring(req.getContextPath().length())
    rootNode.handle(ServletReqContext(rawPath, method, EmptyParams, req, resp))
  }
}
