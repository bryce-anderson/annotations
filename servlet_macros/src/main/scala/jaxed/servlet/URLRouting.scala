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
case class PathPattern(regex: Regex, captureGroupNames: List[String] = Nil, reverse: (Map[String, String] => Option[(String, Map[String, String])])) { parent =>
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

  def +(other: PathPattern): PathPattern = PathPattern(
    new Regex(this.regex.toString + other.regex.toString),
    this.captureGroupNames ::: other.captureGroupNames, { params: Map[String, String] =>
    for {
      (path, params) <- parent.reverse(params)
      (otherPath, params) <- other.reverse(params)
    } yield (path + otherPath, params)
  })
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

    def toPathPattern(allowPartial: Boolean): PathPattern = {
      PathPattern(("^" + regex + (if(allowPartial) "" else "$")).r, captureGroupNames, reverse)
    }

    def +(other: PartialPathPattern): PartialPathPattern = new PartialPathPattern(
      this.regex + other.regex,
      this.captureGroupNames ::: other.captureGroupNames ) {

      override def reverse: (Map[String, String] => Option[(String, Map[String, String])]) = { params: Map[String, String] =>
        for {
          (path, params) <- parent.reverse(params)
          (otherPath, params) <- other.reverse(params)
        } yield (path + otherPath, params)
      }
    }


    def reverse: (Map[String, String] => Option[(String, Map[String, String])]) = {
      if (captureGroupNames.isEmpty){ params:Map[String, String] => Some((regex, params)) }
      else { params: Map[String, String] =>
        val head = captureGroupNames.head
        params.get(head).map {param =>
          (param, params - head )
        }
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
