package jaxed.servlet

/**
 * @author Bryce Anderson
 *         Created on 5/6/13
 */

trait Filter {
  /** Filter method that is executed before attempting to execute the route
    *
    * @param context Request Context propagating the state of the request
    * @return an option representing a result in lue of the result generated by the route, or none of the route should proceeded
    */
  def beforeFilter(context: ServletReqContext): Option[Any] = None

  /** Filter method that is executed after the route
    *
    * This method should always be called in order to possibly intercept the "miss" of a route of desired
    *
    * @param context Request Context propagating the state of the request
    * @param result option representing the success or bypass of the route and or beforeFilter
    * @return body to be processed by the route tree
    */
  def afterFilter(context: ServletReqContext, result: Option[Any]): Option[Any] = result
}
