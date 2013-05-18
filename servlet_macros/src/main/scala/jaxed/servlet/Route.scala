package jaxed.servlet

import jaxed.servlet.ServletReqContext

/**
 * @author Bryce Anderson
 *         Created on 4/19/13
 */

/*
  Trait that represents a single route, eg a GET request or a POST request.
  The route will attempt to run, but errors must be handled by the RouteNode executing the route.
  The return value says whether the route was matched and executed successfully. Exceptions should
  signal that the route matched, but there was a problem of some type during execution.
 */

trait Route {
  def handle(path: ServletReqContext): Option[Any]
  def url(params: Map[String, String] = Map.empty[String, String]): Option[String]
  def pathParamNames: List[String]
}