package jaxed.servlet

import scala.collection.mutable.MutableList
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.language.experimental.macros
import scala.reflect.macros.Context
import jaxed._
import javax.ws.rs.{DELETE, PUT, POST, GET}
import jaxed.servletmacros._
import scala.Some
import scala.Some
import jaxed.servlet._
import scala.Some

/**
 * @author Bryce Anderson
 *         Created on 4/22/13
 */


trait RouteNode extends Route with Filter with PathBuilder { self =>

  def parent: Route
  def path: String

  private val pathPattern = buildPath( path match {
    case "" => ""
    case "/" => ""
    case path if path.startsWith("/") => path
    case path => "/" + path
  }, true)

  protected val routes = new MutableList[Route]()

  def url(params: Map[String, String]) = PathPattern.combine(parent.url, pathPattern.reverse, params)

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

  override def handle(context: ServletReqContext): Option[Any] =
    beforeFilter(context) orElse afterFilter(context, searchLeaves(context))

  def newNode(newPath: String) = {
    val newNode = new RouteNode {
      def path = newPath
      def parent = self
    }
    addRoute(newNode)
    newNode
  }

  def mountBranch(branch: RouteBranch) {
    addRoute(branch)
    branch.mount(self)
  }

  def addRoute(route: Route) { routes += route }

  def addLeafRoute(method: RequestMethod, path: String)(routeMethod: (ServletReqContext) => Any): Route = {
    val pathPattern = buildPath(path, false)
    val route = new Route {
      def url(params: Map[String, String]): Option[String] = PathPattern.combine(self.url, pathPattern.reverse, params)

      def handle(context: ServletReqContext) = if (context.method == method) {
        pathPattern(context.path)
          .map { case (params, _) =>
          routeMethod(context.addParams(params))
        }
      } else None
    }
    addRoute(route)
    route
  }

  def addClassRoute(path: String, routes: Iterable[(RequestMethod, ServletReqContext => Any)]): Route = {
    val pathPattern = buildPath(path, false)

    val route = new Route {
      def handle(context: ServletReqContext): Option[Any] = pathPattern(context.path).flatMap { case (params, _) =>
        routes.find(_._1 == context.method).map{ case (_, f) => f(context.addParams(params)) }
      }

      def url(params: Map[String, String]): Option[String] = PathPattern.combine(self.url, pathPattern.reverse, params)
    }
    addRoute(route)
    route
  }

  def mapClass[A](path: String): Route = macro RouteNode.mapClass_impl[A]
}

object RouteNode {

  type RouteContext = Context { type PrefixType = RouteNode }
  def mapClass_impl[A: c.WeakTypeTag](c: RouteContext) (path: c.Expr[String]) :c.Expr[RouteNode] =  {

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
            classFilterExpr.splice.beforeFilter(reqContextExpr.splice) orElse
              classFilterExpr.splice.afterFilter(reqContextExpr.splice, Some(expr.splice))
          }
        case _ => expr // Don't filter!
      }

      // Make the implementation for route binding routes to RouteNodes
      def bindClass_impl[A: c.WeakTypeTag](node: c.Expr[RouteNode], path: c.Expr[String]): c.Expr[Route] = {
        val paths = genMethodExprs[A, ServletReqContext] // List[(MethodSymbol, c.Expr[(ServletReqContext) => Any])]

        val methodsList = Apply(
          Select(Ident(newTermName("List")), newTermName("apply")),
          paths.map { case (sym, f) =>
          val reqTpe = sym.annotations.find(a => restTypes.exists(_ =:= a.tpe)).get.tpe
          val methodExpr = reqTpe match {
            case t if t =:= typeOf[GET]     => reify(Get)
            case t if t =:= typeOf[POST]    => reify(Post)
            case t if t =:= typeOf[PUT]     => reify(Put)
            case t if t =:= typeOf[DELETE]  => reify(Delete)
          }
          reify{(methodExpr.splice, f.splice)}.tree
        })

        reify {
          node.splice.addClassRoute(path.splice, c.Expr[List[(RequestMethod, ServletReqContext => Any)]](methodsList).splice)
        }

      }
    }

    val expr = servletBinding.bindClass_impl[A](c.prefix.asInstanceOf[servletBinding.c.Expr[RouteNode]],
      path.asInstanceOf[servletBinding.c.Expr[String]])

    println(s"DEBUG: -----------------------------------\n $expr")
    expr.asInstanceOf[c.Expr[RouteNode]]
  }

}

