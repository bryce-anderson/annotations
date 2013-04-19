package jaxmacros

import scala.util.matching.Regex

/**
 * @author Bryce Anderson
 *         Created on 4/19/13 at 8:53 AM
 */

// Modeled after Scalatras path matchers
trait PathPattern {
  def apply(path: String): Option[Map[String, Seq[String]]]
}

object PathHelpers {
  def breakdownPath(in: String): (String, List[String]) = {
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
    (regexBuilder.result, paramsBuilder.result)
  }
}