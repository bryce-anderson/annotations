package jaxed

import rl.UrlCodingUtils._

/**
 * @author Bryce Anderson
 *         Created on 4/20/13
 */
object QueryParams {
  def apply(queryStr: String) =
    queryStr.split('&')
    .map( str =>
      str.indexOf('=') match {
        case -1 => ("", "")  // Simply discard
        case i  => (urlDecode(str.substring(0, i)), urlDecode(str.substring(i+1)))
      }
    ).toMap
}
