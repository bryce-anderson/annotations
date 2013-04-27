package jaxmacros

import scala.collection.mutable.MutableList
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

/**
 * @author Bryce Anderson
 *         Created on 4/22/13
 */

/*
      TODO: Deal with the method types. Strings will be error prone.
   TODO: Does the Option[Any] make sense? Should a proprietary type be created, or an either?
   TODO: What to do about filters? Could be done through overriding handle and renderResponse, but should it be more explicit?
 */

class RouteNode(path: String = "") extends Route with RouteExceptionHandler with ResultRenderer with PathBuilder { self =>



  private val pathPattern = buildPath( path match {
    case "" => ""
    case "/" => ""
    case path if path.startsWith("/") => path
    case path => "/" + path
  }, true)
//    if (path.startsWith("/")) path else {
//      //"/"
//      if(path == "") "" else ("/" + path)
//    }, true)

  protected val routes = new MutableList[Route]()

  // This method should be overridden to match parts of the url.
  override def handle(path: Path, req: HttpServletRequest, resp: HttpServletResponse): Option[Any] = {
    pathPattern(path.path).flatMap{ case (params, subPath) =>
      def searchList(it: Iterator[Route]): Option[Any] = {
        if (it.hasNext) {
          try {
            it.next().handle(path.subPath(subPath, params), req, resp) match {
              case None => searchList(it)
              case done @ Some(Unit) => done
              case Some(result) => self.renderResponse(req, resp, result); Some(Unit)
            }
          }  catch { case t: Throwable => handleException(t, path, req, resp); Some(Unit) }
        } else None
      }

      searchList(routes.iterator)
    }
  }

  def addRoute(route: Route): self.type = { routes += route; self }

  def addLeafRoute(inMethod: RequestMethod, path: String, routeMethod: (RouteParams, HttpServletRequest, HttpServletResponse) => Any): self.type = {
    val pathPattern = self.buildPath(if (path.startsWith("/")) path else "/" + path, false)
    val route = new Route {
      def handle(path: Path, req: HttpServletRequest, resp: HttpServletResponse) = if (path.method == inMethod) {
        pathPattern(path.path)
          .map { case (params, _) =>
          routeMethod(path.params ++ params, req, resp)
        }
      } else None
    }
    addRoute(route)
  }

//  protected def namedRegexMatchToMap(regex: Match) = new Map[String, String] { self =>
//
//    def +[B1 >: String](kv: (String, B1)): Map[String, B1] = self.iterator.toMap + kv
//    def -(key: String): Map[String, String] = self.iterator.toMap - key
//
//    def get(key: String): Option[String] = {
//      try {
//        Some(regex.group(key))
//      } catch {
//        case t: java.util.NoSuchElementException => None
//      }
//    }
//
//    def iterator: Iterator[(String, String)] = regex.groupNames.toIterator.map(key => (key, get(key).get) )
//  }

  def mapClass[A](path: String): RouteNode = macro RouteNode.mapClass_impl[A]
}

object RouteNode {

  def apply(path: String = "") = new RouteNode(path)

//  def mapClass[A](path: String): RouteNode = macro mapFromObject_impl[A]
//
//  def mapFromObject_impl[A: c.WeakTypeTag](c: Context)(path: c.Expr[String]): c.Expr[RouteNode] = {
//    import c.universe._
//    val expr = RouteBinding.bindClass_impl(c)(reify(new RouteNode()), path)
//
//    println(s"DEBUG: -----------------------------------\n $expr")
//    expr
//  }

  type RouteContext = Context { type PrefixType = RouteNode }
  def mapClass_impl[A: c.WeakTypeTag](c: RouteContext) (path: c.Expr[String]) :c.Expr[RouteNode] =  {
    import c.universe._
    val routeExpr = c.Expr[RouteNode](Ident(newTermName("tmp")))
    val expr = RouteBinding.bindClass_impl(c)(c.prefix, path)

    println(s"DEBUG: -----------------------------------\n $expr")
    expr
  }

}

