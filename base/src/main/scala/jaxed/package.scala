/**
 * @author Bryce Anderson
 *         Created on 4/25/13
 */
package object jaxed {

  type Params = Map[String, String]
  val EmptyParams = Map.empty[String, String]

  trait RequestContext {
    def queryParam(name: String): Option[String]
    def routeParam(name: String): Option[String]
    def formParam(name: String): Option[String]
    // def path: String
    // def method: RequestMethod
  }

  // case class Route(paramNames: Set[String], url: String)


  sealed trait RequestMethod
  object Get extends RequestMethod
  object Post extends RequestMethod
  object Put extends RequestMethod
  object Delete extends RequestMethod

}
