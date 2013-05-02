package jaxed

/**
 * @author Bryce Anderson
 *         Created on 5/1/13
 */

trait RequestContext {
  def queryParam(name: String): Option[String]
  def routeParam(name: String): Option[String]
  def formParam(name: String): Option[String]
}
