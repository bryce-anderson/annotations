package jaxed.servlet

import scala.collection.mutable.MutableList
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.language.experimental.macros
import scala.reflect.macros.Context
import jaxed._
import javax.ws.rs.{POST, GET}
import jaxed.servletmacros._
import scala.Some
import scala.Some
import jaxed.servlet._
import scala.Some

/**
 * @author Bryce Anderson
 *         Created on 4/22/13
 */


class RouteNode(path: String = "") extends Route with Filter with PathBuilder { self =>

  private val pathPattern = buildPath( path match {
    case "" => ""
    case "/" => ""
    case path if path.startsWith("/") => path
    case path => "/" + path
  }, true)

  protected val routes = new MutableList[Route]()

  protected def searchLeaves(context: ServletReqContext): Option[Any] = {
    pathPattern(context.path).flatMap{ case (params, subPath) =>
      val subcontext = context.subPath(subPath, params)
      def searchList(it: Iterator[Route]): Option[Any] = {
        if (it.hasNext) {
            it.next().handle(subcontext) match {
              case None => searchList(it)
              case done: Some[_] => done
            }
        } else None
      }

      searchList(routes.iterator)
    }
  }

  override def handle(context: ServletReqContext): Option[Any] = {
    val result = beforeFilter(context) orElse searchLeaves(context)
    afterFilter(context, result)
  }

  def addRoute(route: Route): self.type = { routes += route; self }

  def addRouteLeaf(method: RequestMethod, path: String)(routeMethod: (ServletReqContext) => Any): self.type = {
    val pathPattern = buildPath(path, false)
    val route = new Route {
      def handle(context: ServletReqContext) = if (context.method == method) {
        pathPattern(context.path)
          .map { case (params, _) =>
          routeMethod(context.addParams(params))
        }
      } else None
    }
    addRoute(route)
  }

  def mapClass[A](path: String): RouteNode = macro RouteNode.mapClass_impl[A]
}

object RouteNode {

  def apply(path: String = "") = new RouteNode(path)

  type RouteContext = Context { type PrefixType = RouteNode }
  def mapClass_impl[A: c.WeakTypeTag](c: RouteContext) (path: c.Expr[String]) :c.Expr[RouteNode] =  {
    import c.universe._

    val _c = c
    val servletBinding = new ServletBinding { self =>
      type RT = ServletReqContext
      val c: Context = _c

      import c.universe._


      // Receives the expr representing the execution of the route, right after class instancing
      override def routeExecutionExpr(expr: c.Expr[Any]): c.Expr[Any] = weakTypeOf[A] match {
        case t if t <:< typeOf[Filter] =>
          val classFilterExpr = instExpr.asInstanceOf[c.Expr[Filter]] // We can now use the filter ops!
          reify {
            val result = classFilterExpr.splice.beforeFilter(reqContextExpr.splice) orElse Some(expr.splice)
            classFilterExpr.splice.afterFilter(reqContextExpr.splice, result)
          }
        case _ => expr // Don't filter!
      }

      // Make the implementation for route binding routes to RouteNodes
      def bindClass_impl[A: c.WeakTypeTag](node: c.Expr[RouteNode], path: c.Expr[String]): c.Expr[RouteNode] = {
        val paths = genMethodExprs[A, ServletReqContext] // List[(MethodSymbol, c.Expr[(ServletReqContext) => Any])]

        paths.map { case (sym, f) => (sym.annotations.find(a => restTypes.exists(_ =:= a.tpe)).get.tpe, f) }
          .foldLeft(node){ case (node, (reqTpe, f)) =>
          val methodExpr = reqTpe match {
            case t if t =:= typeOf[GET] => reify(Get)
            case t if t =:= typeOf[POST] => reify(Post)
          }

          reify {
            node.splice.addRouteLeaf(methodExpr.splice, path.splice)(f.splice)
          }
        }
      }
    }

    val expr = servletBinding.bindClass_impl[A](c.prefix.asInstanceOf[servletBinding.c.Expr[RouteNode]],
      path.asInstanceOf[servletBinding.c.Expr[String]])

    println(s"DEBUG: -----------------------------------\n $expr")
    expr.asInstanceOf[c.Expr[RouteNode]]
  }

}

