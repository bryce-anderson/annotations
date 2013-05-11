package jaxed.servlet

/*
This was lifted nearly entirely from the Scalatra project: pathPatternParsers.scala
 */

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import jaxed._

/**
 * A path pattern optionally matches a request path and extracts path
 * parameters.
 */
case class PathPattern(regex: Regex, captureGroupNames: List[String], reverse: ReverseBuilder) { parent =>

  def apply(path: String): Option[(Params, String)] = {
    regex.findFirstMatchIn(path) map { m =>
      var multiParams = Map.empty[String, String]
      var i = 0
      captureGroupNames foreach { name =>
        i += 1
        val value = m.group(i)
        if (value != null) {
          multiParams = multiParams.updated(name, value)
        }
      }
      val remainder = path.substring(m.group(0).length)
      (multiParams, remainder)
    }
  }

  def +(other: PathPattern): PathPattern = {
    val parentStr = {
      val parentStr = parent.regex.toString()
      if (parentStr.endsWith("$")) parentStr.substring(0, parentStr.length-1) else parentStr
    }
    val otherStr = {
      val otherStr = other.regex.toString()
      if (otherStr.startsWith("^")) otherStr.substring(1) else otherStr
    }

    new PathPattern(new Regex(parentStr + otherStr),
      parent.captureGroupNames ::: other.captureGroupNames,
      params =>  PathPattern.combine(parent.reverse, other.reverse, params)
    )
  }
}

/**
 * Parses a string into a path pattern for routing.
 */
trait PathPatternParser {
  def apply(pattern: String, allowPartial: Boolean): PathPattern
}

object PathPatternParser {
  val PathReservedCharacters = "/?#"
}

trait RegexPathPatternParser extends PathPatternParser with RegexParsers {
  /**
   * This parser gradually builds a regular expression.  Some intermediate
   * strings are not valid regexes, so we wait to compile until the end.
   */

  protected case class PartialPathPattern(regex: String, captureGroupNames: List[String] = Nil) { parent =>

    def toPathPattern(allowPartial: Boolean) = PathPattern(
      ("^" + parent.regex + (if(allowPartial) "" else "$")).r,
      parent.captureGroupNames,
      parent.reverse
    )

    def +(other: PartialPathPattern): PartialPathPattern = new PartialPathPattern(
      parent.regex + other.regex,
      parent.captureGroupNames ::: other.captureGroupNames ) {

      override def reverse(params: Map[String, String]) = PathPattern.combine(parent.reverse, other.reverse, params)
    }

    def reverse(params: Map[String, String]): Option[String] = {
      if (captureGroupNames.isEmpty) Some(regex)
      else {
        val head = captureGroupNames.head
        params.get(head)
      }
    }
  }
}

/**
 * A Sinatra-compatible route path pattern parser.
 */
class SinatraPathPatternParser extends RegexPathPatternParser {
  def apply(pattern: String, allowPartial: Boolean): PathPattern =
    parseAll(pathPattern, pattern) match {
      case Success(pathPattern, _) => pathPattern.toPathPattern(allowPartial)
      case _ =>
        throw new IllegalArgumentException("Invalid path pattern: " + pattern)
    }

  private def pathPattern = rep(token) ^^ (_.foldLeft(PartialPathPattern(""))(_+_))

  private def token = splat | namedGroup | normalChar

  private def splat = "*" ^^^ PartialPathPattern("(.*?)", List("splat"))

  private def namedGroup = ":" ~> """\w+""".r ^^
    { groupName => PartialPathPattern("([^/?#]+)", List(groupName)) }

  private def normalChar = "[^:]+".r ^^ { c => PartialPathPattern(c) }
}

object PathPattern {
  def combine(parent: ReverseBuilder,
              child: ReverseBuilder,
              params: Map[String, String]) = {
    for {
      path <- parent(params)
      otherPath <- child(params)
    } yield path + otherPath
  }
}

object SinatraPathPatternParser {
  def apply(pattern: String, allowPartial: Boolean): PathPattern = new SinatraPathPatternParser().apply(pattern, allowPartial)
}

/**
 * RequestContext pattern parser based on Rack::Mount::Strexp, which is used by Rails.
 */
//class RailsPathPatternParser extends RegexPathPatternParser {
//  def apply(pattern: String): PathPattern =
//    parseAll(target, pattern) match {
//      case Success(target, _) => target
//      case _ =>
//        throw new IllegalArgumentException("Invalid path pattern: " + pattern)
//    }
//
//  private def target = expr ^^
//    { e => PartialPathPattern("\\A"+e.regex+"\\Z", e.captureGroupNames).toPathPattern }
//
//  private def expr = rep1(token) ^^
//    { _.reduceLeft { _+_ } }
//
//  private def token = param | glob | optional | static
//
//  private def param = ":" ~> identifier ^^
//    { name => PartialPathPattern("([^#/.?]+)", List(name)) }
//
//  private def identifier = """[a-zA-Z_]\w*""".r
//
//  private def glob = "*" ~> identifier ^^
//    { name => PartialPathPattern("(.+)", List(name)) }
//
//  private def optional: Parser[PartialPathPattern] = "(" ~> expr <~ ")" ^^
//    { e => PartialPathPattern("(?:"+e.regex+")?", e.captureGroupNames) }
//
//  private def static = (escaped | char) ^^
//    { str => PartialPathPattern(str) }
//
//  private def escaped = literal("\\") ~> (char | paren)
//
//  private def char = metachar | stdchar
//
//  private def metachar = """[.^$|?+*{}\\\[\]-]""".r ^^ { "\\"+_ }
//
//  private def stdchar = """[^()]""".r
//
//  private def paren = ("(" | ")") ^^ { "\\"+_ }
//}
//
//object RailsPathPatternParser {
//  def apply(pattern: String): PathPattern = new RailsPathPatternParser().apply(pattern)
//}
