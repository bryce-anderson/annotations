package jaxmacros

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.Request
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.collection.mutable.MutableList

import scala.language.experimental.macros
import scala.reflect.macros.Context

/**
 * @author Bryce Anderson
 *         Created on 4/19/13 at 11:40 AM
 */
class AnnotationHandler extends AbstractHandler with Route { self =>

  private val getRoutes = new MutableList[Route]()
  private val postRoutes = new MutableList[Route]()

  // Virtual method of AbstractHandler
  override def handle(target: String, baseRequest: Request, req: HttpServletRequest, resp: HttpServletResponse) =
    baseRequest.setHandled(handle(req, resp))

  def handle(req: HttpServletRequest, resp: HttpServletResponse): Boolean = {

    def searchList(it: Iterator[Route]): Boolean = {
      if (it.hasNext) {
        if (it.next.handle(req, resp))  true
        else searchList(it)
      } else false
    }

    req.getMethod() match {
      case "GET" => searchList(getRoutes.iterator)
      case "POST" => searchList(postRoutes.iterator)

      case x => throw new NotImplementedError(s"Method type $x not implemented")
    }
  }

  def addRoute(method: String, route: Route): self.type = method match {
    case "GET" => getRoutes += route; self
    case "POST" => postRoutes += route; self

    case x => throw new NotImplementedError(s"Method type $x not implemented")
  }

  def mapClass[A](path: String): AnnotationHandler = macro AnnotationHandler.mapClass_impl[A]
}

object AnnotationHandler {

  def mapClass[A](path: String): AnnotationHandler = macro mapFromObject_impl[A]

  def mapFromObject_impl[A: c.WeakTypeTag](c: Context)(path: c.Expr[String]): c.Expr[AnnotationHandler] =
    RouteBinding.bindClass_impl(c)(c.universe.reify(new AnnotationHandler()), path)

  type RouteContext = Context { type PrefixType = AnnotationHandler }
  def mapClass_impl[A: c.WeakTypeTag](c: RouteContext)
          (path: c.Expr[String]) :c.Expr[AnnotationHandler] =  RouteBinding.bindClass_impl(c)(c.prefix, path)

}
