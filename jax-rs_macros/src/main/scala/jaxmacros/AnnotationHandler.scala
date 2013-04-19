package jaxmacros

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.Request
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.collection.mutable.MutableList
import scala.util.matching.Regex

import scala.language.experimental.macros

/**
 * @author Bryce Anderson
 *         Created on 4/19/13 at 11:40 AM
 */
class AnnotationHandler extends AbstractHandler { self =>

  private val getRoutes = new MutableList[Route]()

  def handle(target: String, baseRequest: Request, req: HttpServletRequest, resp: HttpServletResponse) {
    var regexResults: Option[Regex.Match] = None
    val path = req.getContextPath + req.getServletPath

    req.getMethod() match {
      case "GET" => getRoutes.find(_.handle(req, resp))

      case x => throw new NotImplementedError(s"Method type $x not implemented")
    }
  }

  def addRoute(method: String, route: Route): self.type = method match {
    case "GET" => getRoutes += route; self
  }

  def bindClass[A](path: String) = macro RoutBinding.bindClass_impl[A]
}
