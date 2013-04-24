package jaxmacros

import scala.collection.mutable.MutableList
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.language.experimental.macros
import scala.reflect.macros.Context

/**
 * @author Bryce Anderson
 *         Created on 4/22/13 at 8:56 AM
 */

/*
   TODO: Perhaps the handle should be passed a map of the route params and their values as strings?
            This will allow the "buildup" of the route along the route tree.
            - It comes at the cost of not being able to compile time check the route params for correctness.
            - It does let the mapClass method take non-literal constants which might offer dynamic runtime route building
   TODO: Deal with the method types. Strings will be error prone.
   TODO: Does the Option[Any] make sense? Should a proprietary type be used, or an either?
 */

class RouteNode extends Route with RouteExceptionHandler with ResultRenderer { self =>

  protected val getRoutes = new MutableList[Route]()
  protected val postRoutes = new MutableList[Route]()

  // This method should be overridden to match parts of the url.
  override def handle(path: String, req: HttpServletRequest, resp: HttpServletResponse): Option[Any] = {

    def searchList(it: Iterator[Route]): Option[Any] = {
      if (it.hasNext) {
        try {
          it.next.handle(path, req, resp) match {
            case None => searchList(it)
            case done @ Some(Unit) => done
            case Some(result) => self.renderResponse(req, resp, result); Some(Unit)
          }
        }  catch { case t: Throwable => handleException(t, path, req, resp); Some(Unit) }
      } else None
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

object RouteNode {

  def mapClass[A](path: String): RouteNode = macro mapFromObject_impl[A]

  def mapFromObject_impl[A: c.WeakTypeTag](c: Context)(path: c.Expr[String]): c.Expr[RouteNode] = {
    import c.universe._
    val nodeExpr = c.Expr[RouteNode](Ident(newTermName("node")))
    reify({
      val node = new RouteNode()
      RouteBinding.bindClass_impl(c)(nodeExpr, path).splice
      node
    })

  }

  type RouteContext = Context { type PrefixType = RouteNode }
  def mapClass_impl[A: c.WeakTypeTag](c: RouteContext)
      (path: c.Expr[String]) :c.Expr[RouteNode] =  RouteBinding.bindClass_impl(c)(c.prefix, path)

}

