package jaxmacros

import scala.collection.mutable.MutableList
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.language.experimental.macros
import scala.reflect.macros.Context

/**
 * @author Bryce Anderson
 *         Created on 4/22/13 at 8:56 AM
 */
trait RouteNode extends Route { self =>
  // Need to define the storage for the nodes
  protected def getRoutes: MutableList[Route]
  protected def postRoutes: MutableList[Route]

  override def handle(path: String, req: HttpServletRequest, resp: HttpServletResponse): Boolean = {

    def searchList(it: Iterator[Route]): Boolean = {
      if (it.hasNext) {
        if (it.next.handle(path, req, resp))  true
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

  def mapClass[A](path: String): RouteNode = macro RouteNode.mapClass_impl[A]
}

class DefaultRouteNode extends RouteNode {
  protected val getRoutes = new MutableList[Route]()
  protected val postRoutes = new MutableList[Route]()
}

object RouteNode {

  def mapClass[A](path: String): RouteNode = macro mapFromObject_impl[A]

  def mapFromObject_impl[A: c.WeakTypeTag](c: Context)(path: c.Expr[String]): c.Expr[RouteNode] =
    RouteBinding.bindClass_impl(c)(c.universe.reify(new DefaultRouteNode()), path)

  type RouteContext = Context { type PrefixType = RouteNode }
  def mapClass_impl[A: c.WeakTypeTag](c: RouteContext)
      (path: c.Expr[String]) :c.Expr[RouteNode] =  RouteBinding.bindClass_impl(c)(c.prefix, path)

}

