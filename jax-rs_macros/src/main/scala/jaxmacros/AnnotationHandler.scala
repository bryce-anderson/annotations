package jaxmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

import scala.language.experimental.macros

/**
 * @author Bryce Anderson
 *         Created on 4/19/13
 */
class AnnotationHandler(rootNode: RouteNode) extends HttpServlet { self =>

  // Virtual method of AbstractHandler
  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    val method = req.getMethod match {
      case "GET" => Get
      case "POST" => Post
      case "PUT" => Put
      case "DELETE" => Delete
    }
    val path = Path(req.getPathInfo, EmptyParams, method)
    rootNode.handle(path, req, resp)
  }
}
