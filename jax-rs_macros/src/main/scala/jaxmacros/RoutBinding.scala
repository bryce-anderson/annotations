package jaxmacros

import language.experimental.macros
import scala.reflect.macros.Context
import javax.ws.rs.{GET, POST, DELETE, PUT, HEAD}
import scala.util.matching.Regex
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author Bryce Anderson
 *         Created on 4/18/13 at 2:48 PM
 */
object RouteBinding {

  type RouteContext = Context { type PrefixType = AnnotationHandler }

  def bindClass[A](path: String) = macro bindClass_impl[A]
  def bindClass_impl[A: c.WeakTypeTag](c: RouteContext)(path: c.Expr[String]) :c.Expr[AnnotationHandler] = {
    import c.universe._

    val (regexString: String, params: List[String]) = path.tree match {
      // need to get a regex and a list of param names.
      case Literal(Constant(pathString: String)) =>
        println(s"Found path: $pathString")
        PathHelpers.breakdownPath(pathString)
      case _ => c.error(c.enclosingPosition, "Route path must be a literal constant.")
    }

    println(s"Found path params: $params")
    println(s"Found regex string: $regexString")

    val methodTypes = Set(typeOf[GET], typeOf[POST], typeOf[DELETE], typeOf[PUT], typeOf[HEAD])

    val tpe = weakTypeOf[A]
    val restMethods = tpe.members
      .collect{ case m: MethodSymbol if m.asMethod.annotations.length > 0 => m}
      .filter(m => m.asMethod.annotations.exists(a => methodTypes.exists(_ =:= a.tpe)))
      .toList


    def buildClassRoute(sym: MethodSymbol, regex: String, pathNames: List[String]): c.Expr[Route] = {
      val reqName = "req"
      val reqExpr = c.Expr[HttpServletRequest](Ident(newTermName(reqName)))
      val respName = "resp"
      val respExpr = c.Expr[HttpServletResponse](Ident(newTermName(respName)))

      reify {
        new Route {
          val pathRegex = c.Expr[String](Literal(Constant(regex))).splice.r

          def handle(req: HttpServletRequest, resp: HttpServletResponse): Boolean = {
            req.getPathInfo
            val path = req.getPathInfo

            pathRegex.findFirstMatchIn(path) match {
              case None => false
              case Some(results) =>
                resp.getWriter().append(s"Route matched: $path")
                true
            }
          }
        }
      }
    }

    val restMethodTrees = restMethods.map { m =>
      val params = m.asMethod.paramss
      println(params)
    }

    for (m <- restMethods) {
      println(s"Method named: ${m.name} found with annotations '${m.annotations}'")
    }
    restMethods.head
    reify {
      c.prefix.splice.addRoute("GET", buildClassRoute(restMethods.head, regexString, params).splice)
    }
  }

}
