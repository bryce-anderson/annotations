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
case class MinimalContext(path: String, queryParams: Params = EmptyParams,
                          routeParams: Params = EmptyParams,
                          formParams: Params = EmptyParams, method: RequestMethod)
      extends RequestContext {

  type SelfType = MinimalContext

  def subPath(newPath: String, newParams: jaxed.Params): MinimalContext#SelfType =
    this.copy(path = newPath, routeParams = routeParams ++ newParams)
}

object TestBuilder {
  def buildClass[A] = macro buildClass_impl[A]

  def buildClass_impl[A: c.WeakTypeTag](c: Context): c.Expr[Map[String, (RequestContext => Any)]] = {
    import c.universe._

    val builder = new RouteBinding[MinimalContext, c.type](c)
    val tpe = weakTypeOf[A]

    val methods: List[(MethodSymbol, c.Expr[builder.RouteMethod])] = builder.bindClass_impl[A]


    methods.foldLeft(reify(Map.empty[String, MinimalContext=> Any])) { case (expr, (sym, method)) =>
      reify {
        expr.splice.+(LIT(sym.name.decoded).splice, method.splice)
      }
      ???
    }

    ???
  }
}
