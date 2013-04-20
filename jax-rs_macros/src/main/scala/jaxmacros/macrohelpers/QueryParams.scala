package jaxmacros.macrohelpers

import rl.UrlCodingUtils.urlDecode

/**
 * @author Bryce Anderson
 *         Created on 4/20/13 at 3:04 PM
 */
object QueryParams {
  def apply(queryStr: String) = queryStr.split('&')
    .map( str =>
      str.indexOf('=') match {
        case -1 => ("", "")  // Simply discard
        case i  => (urlDecode(str.substring(0, i)), urlDecode(str.substring(i+1)))
      }
    ).toMap
}
