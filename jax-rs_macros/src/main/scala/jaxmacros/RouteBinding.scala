package jaxmacros

import language.experimental.macros
import scala.reflect.macros.Context
import javax.ws.rs.{GET, POST, DELETE, PUT, FormParam, QueryParam, DefaultValue}
import scala.util.matching.Regex
import jaxed._
import jaxed.QueryParams


/**
 * @author Bryce Anderson
 *         Created on 4/18/13
 */
class RouteBinding[REQUESTCONTEXT <: RequestContext, CONTEXT <: Context](val c: CONTEXT) {

  import c.universe._
  val helpers = new macrohelpers.Helpers[c.type](c)
  import helpers._

  type RouteMethod = (REQUESTCONTEXT) => Any

  def reqContextName = "reqContext"
  def reqContextExpr = c.Expr[REQUESTCONTEXT](Ident(newTermName(reqContextName)))
  def routeParamsName = "routeParams"
  def routeParamsExpr = c.Expr[Params](Select(reqContextExpr.tree, newTermName(routeParamsName)))
  def queryName = "queryParams"
  def queryExpr = c.Expr[Params](Select(reqContextExpr.tree, newTermName(queryName)))
  def formParamsName = "formParams"
  def formParamsExpr = c.Expr[Params](Select(reqContextExpr.tree, newTermName(formParamsName)))

  // Should be overridden to include new types of symbols
  def constructorBuilder(symbol: Symbol, classSym: ClassSymbol, index: Int): Tree = symbol match {
    case p if p.asTerm.isParamWithDefault =>
    reify {
      routeParamsExpr.splice.get(LIT(p.name.decoded).splice).map(primConvert(p.typeSignature).splice)
        .getOrElse(getMethodDefault(Ident(classSym.companionSymbol), "$lessinit$greater", index).splice)
    }.tree

    case p =>
      primConvert(reify(routeParamsExpr.splice.apply(LIT(p.name.decoded).splice)), p.typeSignature).tree
  }

  // TODO: Not implemented yet
  def methodParamBuilder(symbol: c.universe.Symbol, classSym: ClassSymbol, annotation: Type, index: Int): Tree = ???

  //def bindClass[A](handler: RouteNode, path: String) = macro bindClass_impl[A]
  def bindClass_impl[A: c.WeakTypeTag] : List[c.Expr[RouteMethod]] = {


    val methodTypes = List(typeOf[GET], typeOf[POST], typeOf[DELETE], typeOf[PUT])

    val tpe = weakTypeOf[A]
    val TypeRef(_, classSym: ClassSymbol, _: List[Type]) = tpe

    val restMethods = tpe.members.collect{ case m: MethodSymbol =>
      // To deal with a scalac bug
      // https://issues.scala-lang.org/browse/SI-7424
      m.typeSignature // force loading method's signature
      m.annotations.foreach(_.tpe) // force loading all the annotations
      m
    }.collect{ case m: MethodSymbol
        if m.annotations.exists(a => methodTypes.exists(_ =:= a.tpe)) => m }
      .map(m => (m, m.annotations.filter(a => methodTypes.exists(_ =:= a.tpe)).head.tpe))
      .toList


    println("DEBUG: Rest method count: " + restMethods.length.toString)

    def buildMethodRoute(sym: MethodSymbol, requestMethod: Type): c.Expr[RouteMethod] = {

      if (sym.annotations.filter(a => methodTypes.exists(_ =:= a.tpe)).length > 1)
        c.error(c.enclosingPosition, s"Method ${sym.name.decoded} has more than one REST annotation.")

      val pathParams = sym.paramss.map(_.filter { param =>
        println(s"Params: ${param.name} with annotations: ${param.annotations}")
        !param.annotations.exists(a => (a.tpe =:= typeOf[FormParam] || a.tpe =:= typeOf[QueryParam]) )
      }).flatten

      val formParams = sym.paramss.map(_.filter { param =>
        param.annotations.exists(_.tpe =:= typeOf[FormParam])
      }.map(_.name.decoded)).flatten

      val queryParams = sym.paramss.map(_.filter { param =>
        param.annotations.exists(_.tpe =:= typeOf[QueryParam])
      }.map(_.name.decoded)).flatten


      // TODO: add class constructor support
      // Make expr's that will be used to generate an instance of the class if the route matches
      val constructorParams = tpe.member(nme.CONSTRUCTOR).asMethod.paramss.map(_.zipWithIndex.map { case (p, index) =>
        constructorBuilder(p, classSym, index)
//        p match {
//          case p if p.typeSignature =:= typeOf[HttpServletRequest] => reqExpr.tree
//          case p if p.typeSignature =:= typeOf[HttpServletResponse] => respExpr.tree
//          case p if p.asTerm.isParamWithDefault =>
//            reify {
//              routeParamsExpr.splice.get(LIT(p.name.decoded).splice).map(primConvert(p.typeSignature).splice)
//                .getOrElse(getMethodDefault(Ident(classSym.companionSymbol), "$lessinit$greater", index).splice)
//            }.tree
//
//          case p =>
//            primConvert(reify(routeParamsExpr.splice.apply(LIT(p.name.decoded).splice)), p.typeSignature).tree
//        }
      })

      val instExpr = c.Expr[A](Ident(newTermName("clazz")))
      val newInstExpr = c.Expr[A](
        constructorParams.foldLeft[Tree](Select(New(typeArgumentTree(tpe)), nme.CONSTRUCTOR)){ case (tree, params) =>
          Apply(tree, params)
        })

      val methodParamsTree: List[List[Tree]] = sym.paramss.map( _.zipWithIndex.map { case (p, index) =>
//
//        if (p.typeSignature =:= typeOf[HttpServletRequest]) {
//          reqExpr.tree
//        }
//        else if (p.typeSignature =:= typeOf[HttpServletResponse]) {
//          respExpr.tree
//        }
        if (pathParams.exists(_ == p)) {
          primConvert(reify(routeParamsExpr.splice.apply(LIT(p.name.decoded).splice)), p.typeSignature).tree
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

          reify(formParamsExpr.splice.get(LIT(formKey).splice)
              .map(primConvert(p.typeSignature).splice)
              .getOrElse(defaultExpr.splice)).tree
        }

        else methodParamBuilder(p, classSym, requestMethod, index)   // Deal with other possible params
      })

      val routeTree = Apply(Select(instExpr.tree, sym.name), methodParamsTree.flatten)
      val routeResult = c.Expr[Any](routeTree)

      reify {
        new RouteMethod {
          def apply(routeParams: REQUESTCONTEXT): Any = {
            val clazz = newInstExpr.splice   // Name is important, trees depend on it
            routeResult.splice
          }
        }
      }
    }

    // Now we have a list of method expressions representing (REQUESTCONTEXT) => Any.
    // Should we stop here?

//    def addRoute(handler: c.Expr[RouteNode], reqMethod: c.Expr[RequestMethod], methodSymbol: MethodSymbol):c.Expr[RouteNode] = reify {
//      handler.splice.addRouteLeaf(reqMethod.splice, path.splice, buildClassRoute(methodSymbol).splice)
//    }
//
//    def reqMethodExpr(method: String) = c.Expr[RequestMethod](Select(Ident(newTermName("jaxmacros")), newTermName(method)))
//
//    val result = restMethods.foldLeft(handler){ case (handler, (sym, reqMethod)) => reqMethod match {
//      case reqMethod if reqMethod =:= typeOf[GET] => addRoute(handler, reqMethodExpr("Get"), sym)
//      case reqMethod if reqMethod =:= typeOf[POST] => addRoute(handler, reqMethodExpr("Post"), sym)
//    }}
//
//    result
   val result = restMethods.map{case (sym: MethodSymbol, tpe: Type) => buildMethodRoute(sym,tpe)}
    result
  }
}
