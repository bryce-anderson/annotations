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
class AnnotationHandler extends AbstractHandler with RouteNode { self =>

    protected val getRoutes = new MutableList[Route]()
    protected val postRoutes = new MutableList[Route]()

  // Virtual method of AbstractHandler
  override def handle(path: String, baseRequest: Request, req: HttpServletRequest, resp: HttpServletResponse) {
    baseRequest.setHandled(handle(path, req, resp))
  }
}

object AnnotationHandler {
  def mapClass[A](path: String): RouteNode = macro mapFromObject_impl[A]

  def mapFromObject_impl[A: c.WeakTypeTag](c: Context)(path: c.Expr[String]): c.Expr[RouteNode] =
    RouteBinding.bindClass_impl(c)(c.universe.reify(new AnnotationHandler()), path)
}
