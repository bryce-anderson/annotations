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

    def searchList(it: Iterator[Route]): Unit = {
      if (it.hasNext) {
        if (it.next.handle(req, resp))  baseRequest.setHandled(true)
        else searchList(it)
      } else {/* Do nothing, let jetty deal with it. */}
    }

    req.getMethod() match {
      case "GET" => searchList(getRoutes.iterator)

      case x => throw new NotImplementedError(s"Method type $x not implemented")
    }
    baseRequest.setHandled(true)
  }

  def addRoute(method: String, route: Route): self.type = method match {
    case "GET" => getRoutes += route; self
  }

  def bindClass[A](path: String) = macro RouteBinding.bindClass_impl[A]
}
