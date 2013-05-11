package jaxed

/**
 * @author Bryce Anderson
 *         Created on 5/10/13
 */
package object servlet {
  type ReverseBuilder = Map[String, String] => Option[String]
}
