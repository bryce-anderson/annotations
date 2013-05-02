package jaxmacros

import language.experimental.macros
import javax.ws.rs.{GET, POST, DELETE, PUT, FormParam, QueryParam, DefaultValue}
import jaxed._


/**
 * @author Bryce Anderson
 *         Created on 4/18/13
 */
trait RouteBinding extends macrohelpers.Helpers { self =>

  type RT <: RequestContext

  import c.universe._

  lazy val restTypes = List(typeOf[GET], typeOf[POST], typeOf[DELETE], typeOf[PUT])

  private def reqContextName = "reqContext"

  def reqContextExpr = c.Expr[RT](Ident(newTermName(reqContextName)))
  def instExpr = c.Expr(Ident(newTermName("clazz")))

  // Should be overridden and stacked to include new types of symbols
  def constructorBuilder(symbol: Symbol, classSym: ClassSymbol, index: Int): Tree = { //symbol match {
    //case p if p.asTerm.isParamWithDefault =>
    val alternate = {
      if (symbol.asTerm.isParamWithDefault)
        getMethodDefault(Ident(classSym.companionSymbol), "$lessinit$greater", index)
      else reify(throw new java.util.NoSuchElementException("Constructor argument not found"))
    }

    reify {
      reqContextExpr.splice.routeParam(LIT(symbol.name.decoded).splice).map(primConvert(symbol.typeSignature).splice)
        .getOrElse(alternate.splice)
    }.tree
  }

  // TODO: This method should be overridden and stacked to build more complex argument trees
  def methodParamBuilder(symbol: Symbol, annotation: Type, index: Int): Tree = {

    if (symbol.annotations.exists(_.tpe =:= typeOf[QueryParam])) {
      val queryKey = symbol.annotations.find(_.tpe =:= typeOf[QueryParam])
        .map{i => println(s"What are we: ${i.javaArgs}"); i}
        .get.javaArgs.get(newTermName("value"))
        .map(_.toString.replaceAll("\"", ""))
        .getOrElse("")       // Will throw an error during compilation

      val defaultExpr = getDefaultParamExpr(symbol, queryKey, instExpr, symbol.name.decoded, index)

      reify(reqContextExpr.splice.queryParam(LIT(queryKey).splice)
        .map(primConvert(symbol.typeSignature).splice)
        .getOrElse(defaultExpr.splice)).tree
    }
    else if (symbol.annotations.exists(_.tpe == typeOf[FormParam])) {
      val formKey = symbol.annotations.find(_.tpe =:= typeOf[FormParam])
        .get.javaArgs.get(newTermName("value"))
        .map(_.toString.replaceAll("\"", ""))
        .getOrElse("")       // Will throw an error during compilation

      val defaultExpr = getDefaultParamExpr(symbol, formKey, instExpr, symbol.name.decoded, index)

      reify(reqContextExpr.splice.formParam(LIT(formKey).splice)
        .map(primConvert(symbol.typeSignature).splice)
        .getOrElse(defaultExpr.splice)).tree
    } else  {   // Must be a route param
      val defaultExpr = getDefaultParamExpr(symbol, symbol.name.decoded, instExpr, symbol.name.decoded, index)
      reify(reqContextExpr.splice.routeParam(LIT(symbol.name.decoded).splice)
        .map(primConvert(symbol.typeSignature).splice)
        .getOrElse(defaultExpr.splice)
      ).tree
    }
  }

  def genMethodExprs[A: c.WeakTypeTag, RT <: RequestContext: c.WeakTypeTag] : List[(MethodSymbol, c.Expr[(RT) => Any])] = {

    val tpe = weakTypeOf[A]
    val TypeRef(_, classSym: ClassSymbol, _: List[Type]) = tpe
    val TypeRef(_, reqCtxSym: ClassSymbol, _: List[Type]) = weakTypeOf[RT]

    val restMethods = tpe.members.collect{ case m: MethodSymbol =>
      // To deal with a scalac bug
      // https://issues.scala-lang.org/browse/SI-7424
      m.typeSignature               // force loading method's signature
      m.annotations.foreach(_.tpe)  // force loading all the annotations
      m
    }.collect{ case m: MethodSymbol
        if m.annotations.exists(a => restTypes.exists(_ =:= a.tpe)) => m }
      .map(m => (m, m.annotations.filter(a => restTypes.exists(_ =:= a.tpe)).head.tpe))
      .toList


    println("DEBUG: Rest method count: " + restMethods.length.toString)

    def buildMethodRoute(sym: MethodSymbol, requestMethod: Type): c.Expr[(RT) => Any] = {

      if (sym.annotations.filter(a => restTypes.exists(_ =:= a.tpe)).length > 1)
        c.error(c.enclosingPosition, s"Method ${sym.name.decoded} has more than one REST annotation.")

      // Make expr's that will be used to generate an instance of the class if the route matches
      val constructorParams = tpe.member(nme.CONSTRUCTOR).asMethod.paramss
        .map(_.zipWithIndex.map { case (p, index) =>
          constructorBuilder(p, classSym, index)
        })

      val newInstExpr = c.Expr[A](
        constructorParams.foldLeft[Tree](Select(New(typeArgumentTree(tpe)), nme.CONSTRUCTOR)){ case (tree, params) =>
          Apply(tree, params)
        })

      val methodParamsTree: List[List[Tree]] = sym.paramss
        .map( _.zipWithIndex.map { case (p, index) =>
          methodParamBuilder(p, requestMethod, index)
        })

      // TODO: Do we want to flatten the methodParamsTree, or recursively apply like the class constructor
      val routeTree = Apply(Select(instExpr.tree, sym.name), methodParamsTree.flatten)
      val routeResult = c.Expr[Any](routeTree)
      c.Expr[RT => Any](
        Function(List(
          ValDef(Modifiers(Flag.PARAM),
            newTermName(reqContextName),
            Ident(reqCtxSym),
            EmptyTree)
        ), reify{
          val clazz = newInstExpr.splice   // Name is important, trees depend on it
          routeResult.splice
        }.tree)
      )
    }

   val result = restMethods.map{case (sym: MethodSymbol, tpe: Type) => (sym, buildMethodRoute(sym,tpe))}
    result
  }
}
