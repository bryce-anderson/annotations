package jaxed.servlet

import jaxed.servlet.{ServletReqContext, Route, RouteNode}

/**
 * @author Bryce Anderson
 *         Created on 5/19/13
 */

class MockNode(val path: String) extends RouteNode {
  private[servlet] def setParent(node: RouteNode) {}
  def getParent = new RouteNode {

    def getParent(): RouteNode = sys.error("MockNode parent doesn't have a parent")

    private[servlet] def setParent(node: RouteNode) {}
    def path = ""
    override def handle(path: ServletReqContext): Option[Any] = sys.error("Method 'handle' should not be called!")
    override def url(params: Map[String, String]): Option[String] = Some("http://test.com")
  }
}