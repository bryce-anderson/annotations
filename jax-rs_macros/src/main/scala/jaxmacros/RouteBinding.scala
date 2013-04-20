package jaxmacros

import language.experimental.macros
import scala.reflect.macros.Context
import javax.ws.rs.{GET, POST, DELETE, PUT, FormParam, QueryParam}
import scala.util.matching.Regex
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import rl.UrlCodingUtils


/**
 * @author Bryce Anderson
 *         Created on 4/18/13 at 2:48 PM
 */
object RouteBinding {

  type RouteContext = Context { type PrefixType = AnnotationHandler }

  def bindClass[A](path: String) = macro bindClass_impl[A]
  def bindClass_impl[A: c.WeakTypeTag](c: RouteContext)(path: c.Expr[String]) :c.Expr[AnnotationHandler] = {
    import c.universe._

    val helpers = new macrohelpers.Helpers[c.type ](c)
    import helpers._

    val (regexString: String, params: List[String]) = path.tree match {
      // need to get a regex and a list of param names.
      case Literal(Constant(pathString: String)) =>
        println(s"DEBUG: Found path: $pathString")
        PathHelpers.breakdownPath(pathString)
      case _ => c.error(c.enclosingPosition, "Route path must be a literal constant.")
    }

    val methodTypes = List(typeOf[GET], typeOf[POST], typeOf[DELETE], typeOf[PUT])

    val tpe = weakTypeOf[A]

    val restMethods = tpe.members.collect{ case m: MethodSymbol
        if m.annotations.exists(a => methodTypes.exists(_ =:= a.tpe)) => m }.toList

    println(s"DEBUG: Found path params: $params")
    println(s"DEBUG: Found regex string: $regexString")
    println("DEBUG: Rest method count: " + restMethods.length.toString)


    // pathParamNames needs to be in the order found by the regex. Will use it to get regex indexes
    def buildClassRoute(sym: MethodSymbol, regex: String, pathParamNames: List[String]): c.Expr[Route] = {

      val pathParams = sym.paramss.map(_.filter { param =>
        println(s"Params: ${param.name} with annotations: ${param.annotations}")
        !param.annotations.exists(a => (a.tpe =:= typeOf[FormParam] || a.tpe =:= typeOf[QueryParam]) )
      }.map(_.name.decoded)).flatten

      val formParams = sym.paramss.map(_.filter { param =>
        param.annotations.exists(_.tpe =:= typeOf[FormParam])
      }.map(_.name.decoded)).flatten

      val queryParams = sym.paramss.map(_.filter { param =>
        param.annotations.exists(_.tpe =:= typeOf[QueryParam])
      }.map(_.name.decoded)).flatten

      val reqName = "req"
      val reqExpr = c.Expr[HttpServletRequest](Ident(newTermName(reqName)))
      val respName = "resp"
      val respExpr = c.Expr[HttpServletResponse](Ident(newTermName(respName)))
      val resultName = "results"
      val resultExpr = c.Expr[Regex.Match](Ident(newTermName(resultName)))

      if (pathParamNames.sorted != pathParams.sorted)
        c.error(c.enclosingPosition,
          s"Route variables don't match unannotated method variables in method ${sym.name.decoded}.\n" +
          s"Path params: ${pathParamNames}\n" +
          s"Method params: ${pathParams.flatten}")

      val constructorParamsTree: List[List[Tree]] = sym.paramss.map(_.map(_ match {
        case p if pathParams.exists(_ == p.name.decoded) =>
          val index = c.Expr[Int](Literal(Constant(pathParamNames.indexOf(p.name.decoded))))
          reify(resultExpr.splice.group(index.splice + 1)).tree

        case _ => ??? // TODO: need query and form support
      }))

      // TODO: add class constructor support
      val instanceTree = Apply(Select(New(typeArgumentTree(tpe)), nme.CONSTRUCTOR), List())
      val routeTree = Apply(Select(instanceTree, sym.name), constructorParamsTree.flatten)

      val strExpr = c.Expr[String](routeTree)

      reify {
        new Route {
          val pathRegex = c.Expr[String](Literal(Constant(regex))).splice.r

          def handle(req: HttpServletRequest, resp: HttpServletResponse): Boolean = {
            val path = req.getPathInfo

            pathRegex.findFirstMatchIn(path) match {
              case None => false
              case Some(results) => // Should have a RegexMatch of the results to bind.
//                resp.getWriter().append(s"Route matched: $path")
//                println(results.groupCount)
//                (0 to results.groupCount) foreach { i =>
//                  resp.getWriter().append(s"Match: ${results.group(i)}\n")
//                }

                resp.getWriter().write(strExpr.splice)

                true
            }
          }
        }
      }
    }

    val result = reify {
      c.prefix.splice.addRoute("GET", buildClassRoute(restMethods.head, regexString, params).splice)
    }
    println(s"DEBUG: $result")
    result
  }
}
