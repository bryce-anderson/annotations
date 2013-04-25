/**
 * @author Bryce Anderson
 *         Created on 4/25/13
 */
package object jaxmacros {

  type RouteParams = Map[String, String]
  val EmptyParams = Map.empty[String, String]

  case class Path (path: String, params: RouteParams, method: RequestMethod) {
    def subPath(newPath: String, newParams: RouteParams) =
      Path(newPath, if (newParams.isEmpty) params else (params ++ newParams), method)
  }

  sealed trait RequestMethod
  object Get extends RequestMethod
  object Post extends RequestMethod
  object Put extends RequestMethod
  object Delete extends RequestMethod

}
