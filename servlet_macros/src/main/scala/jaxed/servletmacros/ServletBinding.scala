package jaxed
package servletmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import jaxmacros.RouteBinding
import scala.reflect.macros.Context

/**
 * @author Bryce Anderson
 *         Created on 4/30/13
 */
trait ServletBinding extends RouteBinding { self =>
  import c.universe._



  override def constructorBuilder(symbol: Symbol, classSym: ClassSymbol, index: Int): Tree = symbol.typeSignature match {
    case s if s =:= typeOf[HttpServletRequest] =>

    ???
  }
}

object ServletBinding {
  def bindClass_impl(c: Context)(node: c.Expr[RouteNode], path: c.Expr[String]): c.Expr[RouteNode] = {

    val c1 = c

    val servletHelpers = new ServletBinding { val c = c1 }

    ???
  }
}