package jaxmacros

import scala.util.matching.Regex

/**
 * @author Bryce Anderson
 *         Created on 4/24/13 at 10:12 AM
 */
trait PathBuilder { self: RouteNode =>
  // Must build a regex with named groups. This is a default implementation
  def buildRegex(in: String): Regex = {
    val paramsBuilder = new scala.collection.mutable.ListBuffer[String]
    val regexBuilder = new StringBuilder

    def breakdown(in: String): Unit = {
      val groupPattern = """([a-zA-Z0-9\._]+)"""
      in.indexOf(':') match {
        case -1 => regexBuilder.append(in)
        case i  =>
          val (first, second) = in.splitAt(i)
          regexBuilder.append(first)
          regexBuilder.append(groupPattern)
          second.indexOf('/') match {
            case -1 => // Final token
              paramsBuilder += second.substring(1)

            case i =>
              paramsBuilder += second.substring(1,i)
              breakdown(second.substring(i))
          }
      }
    }

    breakdown(in)
    new Regex(regexBuilder.result, paramsBuilder.result:_*)
  }
}
