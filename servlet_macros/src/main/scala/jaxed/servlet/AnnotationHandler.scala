package jaxed.servlet

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

import scala.language.experimental.macros
import jaxed._

/**
 * @author Bryce Anderson
 *         Created on 4/19/13
 */
abstract class AnnotationHandler extends HttpServlet
      with RouteExceptionHandler
      with ResultRenderer
      with DefaultResponses  { self =>

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
    val context = new ServletReqContext(rawPath, method, EmptyParams, req, resp)
    try {
      val result = rootNode.handle(context)
      renderResponse(req, resp, result.getOrElse(on404NotFound(context)))
    } catch {
      case t: Throwable => handleException(t, context)
    }

  }
}
