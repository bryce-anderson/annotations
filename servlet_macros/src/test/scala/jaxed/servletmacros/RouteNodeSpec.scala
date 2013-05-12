package jaxed.servletmacros

import org.specs2.mutable.Specification
import jaxed.servlet.{RouteBranch, AnnotationHandler, ServletReqContext}
import jaxed.Get

/**
 * @author Bryce Anderson
 *         Created on 5/11/13
 */
class RouteNodeSpec extends Specification {
  def ctx(path: String) = new ServletReqContext(path, Get, Map.empty, null, null)

  def handler = {
    new AnnotationHandler {
      object Branch extends RouteBranch("/branch") {
        val route = addLeafRoute(Get, "/leaf") { _ => "Leaf1"}
      }

      mountBranch(Branch)

      addLeafRoute(Get, "/leaf") { _ => "Leaf2"}
    }

  }

  "RouteNode" should {
    "Mount a branch" in {
      handler.handle(ctx("/branch/leaf")) must_== Some("Leaf1")
    }

    "Mount a leaf" in {
      handler.handle(ctx("/leaf")) must_== Some("Leaf2")
    }

    "Miss bad path" in {
      handler.handle(ctx("/nothing")) must_== None
    }
  }


}
