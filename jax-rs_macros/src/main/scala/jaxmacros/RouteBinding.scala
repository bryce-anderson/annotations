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

  def bindClass[A](handler: AnnotationHandler, path: String) = macro bindClass_impl[A]
  def bindClass_impl[A: c.WeakTypeTag](c: Context)
               (handler: c.Expr[AnnotationHandler], path: c.Expr[String]) :c.Expr[AnnotationHandler] = {

    import c.universe._

    val helpers = new macrohelpers.Helpers[c.type ](c)
    import helpers._

    val methodTypes = List(typeOf[GET], typeOf[POST], typeOf[DELETE], typeOf[PUT])

    val (regexString: String, params: List[String]) = path.tree match {
      // need to get a regex and a list of param names.
      case Literal(Constant(pathString: String)) =>
        println(s"DEBUG: Found path: $pathString")
        PathHelpers.breakdownPath(pathString)
      case _ => c.error(c.enclosingPosition, "Route path must be a literal constant.")
    }

    val tpe = weakTypeOf[A]

    val restMethods = tpe.members.collect{ case m: MethodSymbol
        if m.annotations.exists(a => methodTypes.exists(_ =:= a.tpe)) => m }
      .map(m => (m, m.annotations.filter(a => methodTypes.exists(_ =:= a.tpe)).head.tpe))
      .toList

    println(s"DEBUG: Found path params: $params")
    println(s"DEBUG: Found regex string: $regexString")
    println("DEBUG: Rest method count: " + restMethods.length.toString)


    // pathParamNames needs to be in the order found by the regex. Will use it to get regex indexes
    def buildClassRoute(sym: MethodSymbol, regex: String, pathParamNames: List[String]): c.Expr[Route] = {

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
      val resultName = "results"
      val resultExpr = c.Expr[Regex.Match](Ident(newTermName(resultName)))
      val queryName = "queryParams"
      val queryExpr = c.Expr[Map[String, String]](Ident(newTermName(queryName)))

      if (pathParamNames.sorted != pathParams.sorted)
        c.error(c.enclosingPosition,
          s"Route variables don't match unannotated method variables in method ${sym.name.decoded}.\n" +
          s"Path params: ${pathParamNames}\n" +
          s"Method params: ${pathParams.flatten}")

      val constructorParamsTree: List[List[Tree]] = sym.paramss.map(_.map(_ match {
        case p if pathParams.exists(_ == p.name.decoded) =>
          val index = LIT(pathParamNames.indexOf(p.name.decoded))
          primConvert(reify(resultExpr.splice.group(index.splice + 1)), p.typeSignature).tree

        case p if queryParams.exists(_ == p.name.decoded) =>
          val queryKey = p.annotations.find(_.tpe == typeOf[QueryParam])
            .get.javaArgs.apply(newTermName("value")).toString.replaceAll("\"", "")
          val defaultExpr = getDefaultParamExpr(p, queryKey)

          reify(queryExpr.splice.get(LIT(queryKey).splice)
            .map(primConvert(p.typeSignature).splice)
            .getOrElse(defaultExpr.splice)).tree


        case p if formParams.exists(_ == p.name.decoded) =>
          val formKey = p.annotations.find(_.tpe == typeOf[FormParam])
            .get.javaArgs.apply(newTermName("value")).toString.replaceAll("\"", "")
          val defaultExpr = getDefaultParamExpr(p, formKey)

          reify(Option(reqExpr.splice.getParameter(LIT(formKey).splice))
              .map(primConvert(p.typeSignature).splice)
              .getOrElse(defaultExpr.splice)).tree
      }))

      // TODO: add class constructor support
      val instanceTree = Apply(Select(New(typeArgumentTree(tpe)), nme.CONSTRUCTOR), List())
      val routeTree = Apply(Select(instanceTree, sym.name), constructorParamsTree.flatten)

      val strExpr = c.Expr[String](routeTree)

      reify {
        val pathRegex = LIT(regex).splice.r
        new Route {
          def handle( req: HttpServletRequest, resp: HttpServletResponse): Boolean = {
            val path = req.getPathInfo
            lazy val queryParams = macrohelpers.QueryParams(Option(req.getQueryString).getOrElse(""))

            pathRegex.findFirstMatchIn(path) match {
              case None => false
              case Some(results) => // Should have a RegexMatch of the results to bind
                try {
                  resp.getWriter().write(strExpr.splice)
                } catch {
                  case t: Throwable =>
                    t.printStackTrace()// TODO: handle error
                    throw t
                }

                true
            }
          }
        }
      }
    }

    def addRoute(handler: c.Expr[AnnotationHandler], reqMethod: String, methodSymbol: MethodSymbol):c.Expr[AnnotationHandler] = reify (
      handler.splice.addRoute(LIT(reqMethod).splice, buildClassRoute(methodSymbol, regexString, params).splice)
    )

    val result = restMethods.foldLeft(handler){ case (handler, (sym, reqMethod)) => reqMethod match {
      case reqMethod if reqMethod =:= typeOf[GET] => addRoute(handler, "GET", sym)
      case reqMethod if reqMethod =:= typeOf[POST] => addRoute(handler, "POST", sym)
    }}

    println(s"DEBUG: $result")
    result
  }
}
