package jaxed

/**
 * @author Bryce Anderson
 *         Created on 5/6/13
 */

trait Filter {
  def beforeFilter(context: ServletReqContext): Option[Any] = None
  def afterFilter(context: ServletReqContext, result: Option[Any]): Option[Any] = result
}
