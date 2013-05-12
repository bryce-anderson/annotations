package jaxed.servlet

/**
 * @author Bryce Anderson
 *         Created on 5/11/13
 */

class RouteBranch(val path: String) extends RouteNode { self =>

  private var myParent: RouteNode = null

  private[servlet] def mount(newParent: RouteNode) = myParent match {
    case null => myParent = newParent
    case parent => sys.error(s"${self.getClass.getName} already mounted.")
  }

  def parent = Option(myParent).getOrElse(sys.error(s"RouteBranch at '$path' hasn't been mounted."))
}
