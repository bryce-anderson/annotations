package jaxmacros

import language.experimental.macros
import scala.reflect.macros.Context
import javax.ws.rs.{GET, POST, DELETE, PUT, FormParam, QueryParam, DefaultValue}
import scala.util.matching.Regex
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}


/**
 * @author Bryce Anderson
 *         Created on 4/18/13 at 2:48 PM
 */
object RouteBinding {

  type RouteMethod = Function3[Map[String, String], HttpServletRequest, HttpServletResponse, Any]

  def bindClass[A](handler: RouteNode, path: String) = macro bindClass_impl[A]
  def bindClass_impl[A: c.WeakTypeTag](c: Context)
               (handler: c.Expr[RouteNode], path: c.Expr[String]) :c.Expr[RouteNode] = {

    import c.universe._

    val helpers = new macrohelpers.Helpers[c.type](c)
    import helpers._

    val methodTypes = List(typeOf[GET], typeOf[POST], typeOf[DELETE], typeOf[PUT])

    // TODO: could be reintroduced when the route is statically bound to check params.
//    val (regexString: String, params: List[String]) = path.tree match {
//      // need to get a regex and a list of param names.
//      case Literal(Constant(pathString: String)) =>
//        println(s"DEBUG: Found path: $pathString")
//        val (str, params) = PathHelpers.breakdownPath(pathString)
//        println(s"DEBUG: Found path params: $params")
//        println(s"DEBUG: Found regex string: $str")
//        (str, params)
//      case _ => c.error(c.enclosingPosition, "Route path must be a literal constant.")
//    }

    val tpe = weakTypeOf[A]

    val restMethods = tpe.members.collect{ case m: MethodSymbol
        if m.annotations.exists(a => methodTypes.exists(_ =:= a.tpe)) => m }
      .map(m => (m, m.annotations.filter(a => methodTypes.exists(_ =:= a.tpe)).head.tpe))
      .toList


    println("DEBUG: Rest method count: " + restMethods.length.toString)


    // pathParamNames needs to be in the order found by the regex. Will use it to get regex indexes
    def buildClassRoute(sym: MethodSymbol): c.Expr[RouteMethod] = {

      if (sym.annotations.filter(a => methodTypes.exists(_ =:= a.tpe)).length > 1)
        c.error(c.enclosingPosition, s"Method ${sym.name.decoded} has more than one REST annotation.")

      val pathParams = sym.paramss.map(_.filter { param =>
        println(s"Params: ${param.name} with annotations: ${param.annotations}")
        !param.annotations.exists(a => (a.tpe =:= typeOf[FormParam] || a.tpe =:= typeOf[QueryParam]) )
      }.map(_.name.decoded)).flatten

      val formParams = sym.paramss.map(_.filter { param =>
        param.annotations.exists(_.tpe =:= typeOf[FormParam])
      }.map(_.name.decoded)).flatten

      if (!sym.annotations.exists(_.tpe == typeOf[POST]) && formParams.length > 0)
        c.error(c.enclosingPosition, s"Method '${sym.name.decoded}' has POST params but is not a POST request.")

      val queryParams = sym.paramss.map(_.filter { param =>
        param.annotations.exists(_.tpe =:= typeOf[QueryParam])
      }.map(_.name.decoded)).flatten

      val reqName = "req"
      val reqExpr = c.Expr[HttpServletRequest](Ident(newTermName(reqName)))
      val respName = "resp"
      val respExpr = c.Expr[HttpServletResponse](Ident(newTermName(respName)))
      val routeParamsName = "routeParams"
      val routeParamstExpr = c.Expr[Map[String, String]](Ident(newTermName(routeParamsName)))
      val queryName = "queryParams"
      val queryExpr = c.Expr[Map[String, String]](Ident(newTermName(queryName)))

//      if (pathParamNames.sorted != pathParams.sorted)
//        c.error(c.enclosingPosition,
//          s"Route variables don't match unannotated method variables in method ${sym.name.decoded}.\n" +
//          s"Path params: ${pathParamNames}\n" +
//          s"Method params: ${pathParams.flatten}")

      // TODO: add class constructor support
      // Make expr's that will be used to generate an instance of the class if the route matches
      val instExpr = c.Expr[A](Ident(newTermName("clazz")))
      val newInstExpr = c.Expr[A](Apply(Select(New(typeArgumentTree(tpe)), nme.CONSTRUCTOR), List()))

      val constructorParamsTree: List[List[Tree]] = sym.paramss.map( _.zipWithIndex.map { case (p, index) =>
        if (pathParams.exists(_ == p.name.decoded)) {
          primConvert(reify(routeParamstExpr.splice.apply(LIT(p.name.decoded).splice)), p.typeSignature).tree
        }

        else if (queryParams.exists(_ == p.name.decoded)) {
          val queryKey = p.annotations.find(_.tpe == typeOf[QueryParam])
            .get.javaArgs.apply(newTermName("value")).toString.replaceAll("\"", "")
          val defaultExpr = getDefaultParamExpr(p, queryKey, instExpr, sym.name.decoded, index)

          reify(queryExpr.splice.get(LIT(queryKey).splice)
            .map(primConvert(p.typeSignature).splice)
            .getOrElse(defaultExpr.splice)).tree
        }

        else if (formParams.exists(_ == p.name.decoded)) {
          val formKey = p.annotations.find(_.tpe == typeOf[FormParam])
            .get.javaArgs.apply(newTermName("value")).toString.replaceAll("\"", "")
          val defaultExpr = getDefaultParamExpr(p, formKey, instExpr, sym.name.decoded, index)

          reify(Option(reqExpr.splice.getParameter(LIT(formKey).splice))
              .map(primConvert(p.typeSignature).splice)
              .getOrElse(defaultExpr.splice)).tree
        }
        else {???}
      })

      val routeTree = Apply(Select(instExpr.tree, sym.name), constructorParamsTree.flatten)
      val routeResult = c.Expr[Any](routeTree)

      reify {
        new RouteMethod {
          def apply(routeParams: Map[String, String], req: HttpServletRequest, resp: HttpServletResponse): Any = {
            lazy val queryParams = macrohelpers.QueryParams(Option(req.getQueryString).getOrElse(""))
            val clazz = newInstExpr.splice   // Name is important, trees depend on it
            routeResult.splice
          }
        }
      }
    }

    def addRoute(handler: c.Expr[RouteNode], reqMethod: String, methodSymbol: MethodSymbol):c.Expr[RouteNode] = reify {
      handler.splice.addRoute(LIT(reqMethod).splice, path.splice, buildClassRoute(methodSymbol).splice)
    }

    val result = restMethods.foldLeft(handler){ case (handler, (sym, reqMethod)) => reqMethod match {
      case reqMethod if reqMethod =:= typeOf[GET] => addRoute(handler, "GET", sym)
      case reqMethod if reqMethod =:= typeOf[POST] => addRoute(handler, "POST", sym)
    }}

    result
  }
}
