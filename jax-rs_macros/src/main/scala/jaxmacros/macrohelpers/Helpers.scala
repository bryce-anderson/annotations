package jaxmacros.macrohelpers

import scala.reflect.macros.Context

/**
 * @author brycea
 *         Created on 4/20/13 at 1:11 PM
 */
class Helpers[C <: Context](val c1: C) {
  import c1.universe._

  // Gives the type needed to instance the class. Will only be handy later when the type is more complex
  def typeArgumentTree(t: Type): Tree = t match {
    case TypeRef(_, _, typeArgs @ _ :: _) => AppliedTypeTree(
      Ident(t.typeSymbol), typeArgs map (t => typeArgumentTree(t)) )
    case _                                => Ident(t.typeSymbol.name)
  }

}
