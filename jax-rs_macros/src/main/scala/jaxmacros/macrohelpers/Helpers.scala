package jaxmacros.macrohelpers

import scala.reflect.macros.Context
import javax.ws.rs.DefaultValue

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

  def LIT[A](in: A) = c1.Expr[A](Literal(Constant(in)))

  def PRIM(str: String, tpe: c1.Type): c1.Expr[_] = {
    try {
      tpe match {
        case t if t =:= typeOf[Int] => LIT(str.toInt)
        case t if t =:= typeOf[Long] => LIT(str.toLong)
        case t if t =:= typeOf[Double] => LIT(str.toDouble)
        case t if t =:= typeOf[Float] => LIT(str.toFloat)
        case t if t =:= typeOf[String] => LIT(str)
      }
    } catch {
      case t: java.lang.NumberFormatException =>
        val err = s"Cannot convert value '$str' to type ${tpe}"
        c1.error(c1.enclosingPosition, err)
        throw new java.lang.IllegalArgumentException(err)
    }
  }

  def primConvert(str: c1.Expr[String], tpe: c1.Type) = tpe match {
    case t if tpe =:= typeOf[Int] => reify(Converters.strToInt(str.splice))
    case t if tpe =:= typeOf[Long] => reify(Converters.strToLong(str.splice))
    case t if tpe =:= typeOf[Float] => reify(Converters.strToFloat(str.splice))
    case t if tpe =:= typeOf[Double] => reify(Converters.strToDouble(str.splice))
    case t if tpe =:= typeOf[String] => str
  }

  def primConvert(tpe: c1.Type) = tpe match {
    case t if tpe =:= typeOf[Int] => reify(Converters.strToInt(_))
    case t if tpe =:= typeOf[Long] => reify(Converters.strToLong(_))
    case t if tpe =:= typeOf[Float] => reify(Converters.strToFloat(_))
    case t if tpe =:= typeOf[Double] => reify(Converters.strToDouble(_))
    case t if tpe =:= typeOf[String] => reify(Converters.strToStr(_))
  }

  def getDefaultParamExpr(p: Symbol, name: String) = p.annotations.find(_.tpe == typeOf[DefaultValue])
    .map(_.javaArgs.apply(newTermName("value")).toString.replaceAll("\"", ""))
    .map(PRIM(_, p.typeSignature))
    .getOrElse(reify(throw new IllegalArgumentException(s"missing query param: ${LIT(name).splice}")))
}

object Converters {
  def strToInt(str: String) = str.toInt
  def strToLong(str: String) = str.toLong
  def strToDouble(str: String) = str.toDouble
  def strToFloat(str: String) = str.toFloat
  def strToStr(str: String) = str
}
