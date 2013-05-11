package jaxed
package servletmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import jaxmacros.RouteBinding
import scala.reflect.macros.Context
import javax.ws.rs.{CookieParam, POST, GET}
import jaxed.servlet.ServletReqContext

/**
 * @author Bryce Anderson
 *         Created on 4/30/13
 */
trait ServletBinding extends RouteBinding { self =>

  type RT <: ServletReqContext

  import c.universe._

  private def reqRespTree(symbol: Symbol, parentSymbol: Symbol, index: Int) = symbol match {
    case s if s.typeSignature =:= typeOf[HttpServletRequest] => Some(reify(reqContextExpr.splice.req).tree)
    case s if s.typeSignature =:= typeOf[HttpServletResponse]=> Some(reify(reqContextExpr.splice.resp).tree)
    case s if !getAnnotation[CookieParam](s).isEmpty =>
      val cookieName = getAnnotation[CookieParam](s).get.javaArgs(newTermName("value")).toString.replaceAll("\"", "")
      val defaultExpr = parentSymbol match {
        case classSym: ClassSymbol =>
          //getMethodDefault(Ident(classSym.companionSymbol), "$lessinit$greater", index)
          getDefaultParamExpr(symbol, symbol.name.encoded, Ident(classSym.companionSymbol), "$lessinit$greater", index)
        case methSym: MethodSymbol => getDefaultParamExpr(symbol, symbol.name.encoded, self.instExpr.tree, methSym.name.encoded, index)
      }

      Some(reify(reqContextExpr.splice
        .getCookie(LIT(cookieName).splice)
        .map(primConvert(symbol.typeSignature).splice)
        .getOrElse(defaultExpr.splice)).tree
      )

    case _ => None
  }

  override def constructorBuilder(paramSym: Symbol, classSym: ClassSymbol, index: Int): Tree =
    reqRespTree(paramSym, classSym, index) getOrElse super.constructorBuilder(paramSym, classSym, index)

  override def methodParamBuilder(paramSym: Symbol, methodSymbol: MethodSymbol, index: Int): Tree =
    reqRespTree(paramSym, methodSymbol, index) getOrElse super.methodParamBuilder(paramSym, methodSymbol, index)

}