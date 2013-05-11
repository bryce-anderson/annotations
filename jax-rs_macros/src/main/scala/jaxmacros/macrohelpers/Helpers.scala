package jaxmacros.macrohelpers

import scala.reflect.macros.Context
import javax.ws.rs.DefaultValue

/**
 * @author Bryce Anderson
 *         Created on 4/20/13
 */
trait Helpers { self =>
  val c: Context
  import c.universe._

  // Gives the type needed to instance the class. Will only be handy later when the type is more complex
  def typeArgumentTree(t: Type): Tree = t match {
    case TypeRef(_, _, typeArgs @ _ :: _) => AppliedTypeTree(
      Ident(t.typeSymbol), typeArgs map (t => typeArgumentTree(t)) )
    case _                                => Ident(t.typeSymbol.name)
  }

  def LIT[A](in: A) = c.Expr[A](Literal(Constant(in)))

  def PRIM(str: String, tpe: c.Type): c.Expr[_] = {
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
        c.error(c.enclosingPosition, err)
        throw new java.lang.IllegalArgumentException(err)
    }
  }

  def primConvert(str: c.Expr[String], tpe: c.Type) = tpe match {
    case t if tpe =:= typeOf[Int] => reify(Converters.strToInt(str.splice))
    case t if tpe =:= typeOf[Long] => reify(Converters.strToLong(str.splice))
    case t if tpe =:= typeOf[Float] => reify(Converters.strToFloat(str.splice))
    case t if tpe =:= typeOf[Double] => reify(Converters.strToDouble(str.splice))
    case t if tpe =:= typeOf[String] => str
    case t =>
      c.error(c.enclosingPosition, s"type '$tpe' is not a primitive.")
      throw new java.util.UnknownFormatConversionException(s"Cannot convert type $tpe")
  }

  def primConvert(tpe: c.Type) = tpe match {
    case t if tpe =:= typeOf[Int] => reify(Converters.strToInt(_))
    case t if tpe =:= typeOf[Long] => reify(Converters.strToLong(_))
    case t if tpe =:= typeOf[Float] => reify(Converters.strToFloat(_))
    case t if tpe =:= typeOf[Double] => reify(Converters.strToDouble(_))
    case t if tpe =:= typeOf[String] => reify(Converters.strToStr(_))
    case t =>
      c.error(c.enclosingPosition, s"type '$tpe' is not a primitive.")
      throw new java.util.UnknownFormatConversionException(s"Cannot convert type $tpe")
  }

  def getAnnotation[T: TypeTag](sym: Symbol) = sym.annotations.find(_.tpe =:= typeOf[T])

  def getDefaultParamExpr(p: Symbol, name: String, classTree: Tree, methodName: String, paramIndex: Int) = {
    p.annotations.find(_.tpe == typeOf[DefaultValue])
      .map(_.javaArgs.apply(newTermName("value")).toString.replaceAll("\"", ""))
      .map(PRIM(_, p.typeSignature))
      .orElse(p.asTerm.isParamWithDefault match {
      case true =>  Some(getMethodDefault(classTree, methodName, paramIndex))
      case false => None
    })
      .getOrElse(reify(throw new IllegalArgumentException(s"missing param: ${LIT(name).splice}")))
  }

  def getMethodDefault(classTree: Tree, methodName: String, paramIndex: Int) = c.Expr(
    Select(classTree, // TODO: find canonical way to get default method names
      newTermName(methodName + "$default$" + (paramIndex + 1).toString))
  )
}

object Converters {
  def strToInt(str: String) = str.toInt
  def strToLong(str: String) = str.toLong
  def strToDouble(str: String) = str.toDouble
  def strToFloat(str: String) = str.toFloat
  def strToStr(str: String) = str
}
