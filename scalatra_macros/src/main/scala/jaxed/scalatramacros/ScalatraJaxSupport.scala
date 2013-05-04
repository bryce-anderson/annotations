package jaxed.scalatramacros

import org.scalatra.{SinatraRouteMatcher, Route, ScalatraServlet}

import scala.reflect.macros.Context
import language.experimental.macros
import jaxed.servletmacros.{ServletReqContext, ServletBinding}
import javax.ws.rs.{POST, GET}
import jaxed.{Post, Get}

/**
 * @author Bryce Anderson
 *         Created on 5/3/13
 */
trait ScalatraJaxSupport extends ScalatraServlet  { self =>
  protected def __self = self  // Needed for the macro to resolve itself. May be removed if we don't use reify...

  def bindClass[A](path: String): Unit = macro ScalatraJaxSupport.bindClass_impl[A]

}

object ScalatraJaxSupport {

  def bindClass_impl[A: c.WeakTypeTag](c: Context)(path: c.Expr[String]): c.Expr[Unit] = {

    val c1 = c
    val binder = new ServletBinding {
      type RT = ServletReqContext
      val c = c1

      import c.universe._

      def buildBlock[A: WeakTypeTag](path: c.Expr[String], scalatra: c.Expr[ScalatraServlet]): List[Tree] = {
        val routes = genMethodExprs[A, ServletReqContext]  // List[(MethodSymbol, ServletReqContext => Any)]
        .map { case (sym, f) =>
          val reqTpe = sym.annotations.find(a => restTypes.exists(_ =:= a.tpe)).get.tpe
          reqTpe match {
            case t if t =:= typeOf[GET] => reify {
                scalatra.splice.get(new SinatraRouteMatcher(path.splice)) {   // TODO: can we make this expression know its in the trait, of does it have to be done with trees explicitly?
                  val ctx = ServletReqContext(path.splice, Get, scalatra.splice.params(scalatra.splice.request), scalatra.splice.request, scalatra.splice.response)
                  f.splice.apply(ctx)
                }
              }

            case t if t =:= typeOf[POST] => reify {
              scalatra.splice.post(new SinatraRouteMatcher(path.splice)) {   // TODO: can we make this expression know its in the trait, of does it have to be done with trees explicitly?
              val ctx = ServletReqContext(path.splice, Post, scalatra.splice.params(scalatra.splice.request), scalatra.splice.request, scalatra.splice.response)
                f.splice.apply(ctx)
              }
            }
          }
        }
        routes.map(_.tree)
      }
    }

    import c.universe._

    val scalatraExpr = c.Expr[ScalatraJaxSupport](Ident(newTermName("__self")))
    c.Expr[Unit](Block(
      binder.buildBlock[A](path.asInstanceOf[binder.c.Expr[String]], scalatraExpr.asInstanceOf[binder.c.Expr[ScalatraServlet]])
        .asInstanceOf[List[Tree]],               // Damn path dependent types
      Literal(Constant())
    ))
  }
}
