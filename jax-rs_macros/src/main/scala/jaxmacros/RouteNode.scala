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

  private val regex = buildRegex(if (path.startsWith("/")) path else {
    if(path == "") "" else ("/" + path)
  })

  protected val getRoutes = new MutableList[Route]()
  protected val postRoutes = new MutableList[Route]()

  // This method should be overridden to match parts of the url.
  override def handle(path: String, pathParams: Map[String, String], req: HttpServletRequest, resp: HttpServletResponse): Option[Any] = {

    regex.findFirstMatchIn(path).flatMap{ result =>
      val subPathParams = if (result.groupCount != 0) {
        pathParams ++ namedRegexMatchToMap(result)
      } else pathParams

      val subPath = path.substring(result.end)

      def searchList(it: Iterator[Route]): Option[Any] = {
        if (it.hasNext) {
          try {
            it.next().handle(subPath, subPathParams, req, resp) match {
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
  }

  def addRoute(method: String, route: Route): self.type = {
    method match {
      case "GET" => getRoutes += route
      case "POST" => postRoutes += route

      case x => throw new NotImplementedError(s"Method type $x not implemented")
    }
    self
  }

  def addRoute(method: String, path: String, routeMethod: (Map[String, String], HttpServletRequest, HttpServletResponse) => Any): self.type = {
    val regex = self.buildRegex(if (path.startsWith("/")) path else "/" + path)
    val route = new Route {
        def handle(path: String, routeParams: Map[String, String], req: HttpServletRequest, resp: HttpServletResponse) =
          regex.findFirstMatchIn(path)
            .map { matches =>
            routeMethod(routeParams ++ namedRegexMatchToMap(matches), req, resp)
          }
      }
    addRoute(method, route)
  }

  protected def namedRegexMatchToMap(regex: Match) = new Map[String, String] { self =>

    def +[B1 >: String](kv: (String, B1)): Map[String, B1] = self.iterator.toMap + kv
    def -(key: String): Map[String, String] = self.iterator.toMap - key

    def get(key: String): Option[String] = {
      try {
        Some(regex.group(key))
      } catch {
        case t: java.util.NoSuchElementException => None
      }
    }

    def iterator: Iterator[(String, String)] = regex.groupNames.toIterator.map(key => (key, get(key).get) )
  }

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

