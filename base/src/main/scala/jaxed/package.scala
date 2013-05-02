/**
 * @author Bryce Anderson
 *         Created on 4/25/13
 */
package object jaxed {

  type Params = Map[String, String]
  val EmptyParams = Map.empty[String, String]

  // case class Route(paramNames: Set[String], url: String)


  sealed trait RequestMethod
  object Get extends RequestMethod
  object Post extends RequestMethod
  object Put extends RequestMethod
  object Delete extends RequestMethod

}
