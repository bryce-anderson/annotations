package jaxmacros

import scala.util.matching.Regex

/**
 * @author Bryce Anderson
 *         Created on 4/24/13
 */
trait PathBuilder {
  // Must build a regex with named groups. This is a default implementation
  def buildPath(in: String, allowPartial: Boolean): PathPattern =
    SinatraPathPatternParser.apply(in, allowPartial)
}
