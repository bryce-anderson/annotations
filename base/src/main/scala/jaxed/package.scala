/**
 * @author Bryce Anderson
 *         Created on 4/25/13
 */
package object jaxed {

  type Params = Map[String, String]
  val EmptyParams = Map.empty[String, String]


  trait RequestContext {
    type SelfType <: RequestContext
    def path: String
    def queryParams: Params
    def routeParams: Params
    def formParams: Params
    def method: RequestMethod
    def subPath(newPath: String, newParams: Params): SelfType
  }

  case class Route(paramNames: Set[String], url: String)

//  class RequestContext(val path: String, val params: RouteParams, val method: RequestMethod)
//        extends RequestContextLike[RequestContext] {
//    def subPath(newPath: String, newParams: RouteParams) =
//      new RequestContext(newPath, if (newParams.isEmpty) params else (params ++ newParams), method)
//  }

  sealed trait RequestMethod
  object Get extends RequestMethod
  object Post extends RequestMethod
  object Put extends RequestMethod
  object Delete extends RequestMethod

}
