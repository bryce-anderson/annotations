package jaxed
package servletmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import jaxmacros.RouteBinding
import scala.reflect.macros.Context
import javax.ws.rs.{POST, GET}

/**
 * @author Bryce Anderson
 *         Created on 4/30/13
 */
trait ServletBinding extends RouteBinding { self =>

  type RT <: ServletReqContext

  import c.universe._

  private def reqRespTree(symbol: Symbol) = symbol.typeSignature match {
    case s if s =:= typeOf[HttpServletRequest] => Some(reify(reqContextExpr.splice.req).tree)
    case s if s =:= typeOf[HttpServletResponse]=> Some(reify(reqContextExpr.splice.resp).tree)
    case _ => None
  }

  override def constructorBuilder(paramSym: Symbol, classSym: ClassSymbol, index: Int): Tree =
    reqRespTree(paramSym) getOrElse super.constructorBuilder(paramSym, classSym, index)

  override def methodParamBuilder(paramSym: Symbol, methodSymbol: MethodSymbol, annotation: Type, index: Int): Tree =
    reqRespTree(paramSym) getOrElse super.methodParamBuilder(paramSym, methodSymbol, annotation, index)


  def bindClass_impl[A: c.WeakTypeTag](node: c.Expr[RouteNode], path: c.Expr[String]): c.Expr[RouteNode] = {
    val paths = genMethodExprs[A, ServletReqContext] // List[(MethodSymbol, c.Expr[(ServletReqContext) => Any])]

    paths.map { case (sym, f) => (sym.annotations.find(a => restTypes.exists(_ =:= a.tpe)).get.tpe, f) }
      .foldLeft(node){ case (node, (reqTpe, f)) =>
        val methodExpr = reqTpe match {
          case t if t =:= typeOf[GET] => reify(Get)
          case t if t =:= typeOf[POST] => reify(Post)
        }
        reify {
          node.splice.addRouteLeaf(methodExpr.splice, path.splice, f.splice)
        }
      }
  }
}