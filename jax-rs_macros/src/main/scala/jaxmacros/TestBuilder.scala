package jaxmacros

import language.experimental.macros
import scala.reflect.macros.Context
import jaxed.{RequestMethod, RequestContext, Params, EmptyParams}

/**
 * @author Bryce Anderson
 *         Created on 5/1/13
 */

/*
Bootstrap the package, not doing much with this default type
 */
case class MinimalContext(path: String, method: RequestMethod, queryParams: Params = EmptyParams,
                          routeParams: Params = EmptyParams,
                          formParams: Params = EmptyParams)
      extends RequestContext {

  type SelfType = MinimalContext

  def subPath(newPath: String, newParams: jaxed.Params): MinimalContext#SelfType =
    this.copy(path = newPath, routeParams = routeParams ++ newParams)
}

object TestBuilder {
  def buildClass[A]: Map[String, (MinimalContext => Any)] = macro buildClass_impl[A]

  def buildClass_impl[A: c.WeakTypeTag](c: Context): c.Expr[Map[String, (MinimalContext => Any)]] = {
    import c.universe._

    val c1 = c   // Need to rename for making the builder, else get recursive value
    val builder = new RouteBinding{  val c = c1 }
    import builder.LIT

    val methods = builder.genMethodExprs[A, MinimalContext] // List[(name: String, method: MinimalContext => Any)]

    val mapExpr = methods.foldLeft(reify(Map.empty[String, MinimalContext=> Any])) { case (expr, (sym, method)) =>
      reify {
        expr.splice.+((LIT(sym.name.decoded).splice, method.splice))
      }
    }
    println (mapExpr)
    mapExpr
  }
}
