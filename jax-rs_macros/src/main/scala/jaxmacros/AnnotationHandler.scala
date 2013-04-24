package jaxmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import scala.collection.mutable.MutableList

import scala.language.experimental.macros
import scala.reflect.macros.Context

/**
 * @author Bryce Anderson
 *         Created on 4/19/13 at 11:40 AM
 */
class AnnotationHandler(rootNode: RouteNode) extends HttpServlet { self =>

  // Virtual method of AbstractHandler
  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    rootNode.handle(req.getPathInfo, req, resp)
  }
}

//object AnnotationHandler {
//  def mapClass[A](path: String): AnnotationHandler = macro mapFromObject_impl[A]
//
//  def mapFromObject_impl[A: c.WeakTypeTag](c: Context)(path: c.Expr[String]): c.Expr[AnnotationHandler] = {
//    import c.universe._
//
//    val handlerExpr = c.Expr[AnnotationHandler](Ident(newTermName("handler")))
//    val expr = reify (
//      {
//        val handler = new AnnotationHandler()
//        (RouteBinding.bindClass_impl(c)(handlerExpr, path)).splice
//        handler
//      }
//    )
//    //println("----------------------------------------\n" + expr.toString)
//    expr
//  }
//}
