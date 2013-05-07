package jaxed.scalatramacros

import org.scalatra.{SinatraRouteMatcher, Route, ScalatraServlet}

import scala.reflect.macros.Context
import language.experimental.macros
import jaxed.servletmacros.{ServletBinding}
import javax.ws.rs.{POST, GET}
import jaxed.{ServletReqContext, Post, Get}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author Bryce Anderson
 *         Created on 5/3/13
 */
trait ScalatraJaxSupport extends ScalatraServlet  { self =>
  def bindClass[A](path: String): Unit = macro ScalatraJaxSupport.bindClass_impl[A]
}
    // TODO: make ScalatraReqContext which uses the right request, response, etc.
object ScalatraJaxSupport {

  def bindClass_impl[A: c.WeakTypeTag](c: Context)(path: c.Expr[String]): c.Expr[Unit] = {

    val c1 = c
    val binder = new ServletBinding {
      type RT = ScalatraReqContext
      val c = c1

      import c.universe._

      def buildBlock[A: WeakTypeTag](path: c.Expr[String]): List[Tree] = {
        val routes = genMethodExprs[A, ServletReqContext]  // List[(MethodSymbol, ServletReqContext => Any)]
        .map { case (sym, f) =>
          val reqTpe = sym.annotations.find(a => restTypes.exists(_ =:= a.tpe)).get.tpe
          val (reqTpeExpr,idTree) = reqTpe match {
            case t if t =:= typeOf[GET] => (reify(Get), Ident(newTermName("get")))
            case t if t =:= typeOf[POST] => (reify(Post), Ident(newTermName("post")))
          }

          val paramsExpr = c.Expr[org.scalatra.Params](Ident(newTermName("params")))
          val reqExpr = c.Expr[HttpServletRequest](Ident(newTermName("request")))
          val respExpr = c.Expr[HttpServletResponse](Ident(newTermName("response")))
          val bodyExpr = reify{
            val ctx = new ScalatraReqContext(reqTpeExpr.splice, paramsExpr.splice, reqExpr.splice, respExpr.splice)
            f.splice.apply(ctx)
          }
          Apply(Apply(idTree, List(path.tree)), List(bodyExpr.tree))  // Apply path and generated body
        }
        routes
      }
    }

    import c.universe._

    val result = c.Expr[Unit](Block(
      binder.buildBlock[A](path.asInstanceOf[binder.c.Expr[String]])
        .asInstanceOf[List[Tree]],               // Damn path dependent types
      Literal(Constant())
    ))

    println(s"DEBUG: ------------------------\n$result")
    result
  }
}
