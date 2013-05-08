package jaxed.servlet

import jaxed.servlet.ServletReqContext

/**
 * @author Bryce Anderson
 *         Created on 4/22/13
 */
trait RouteExceptionHandler {
  // This method should be stacked using super calls to created an exception pipeline.
  protected def handleException(t: Throwable, context: ServletReqContext) {
    import context.resp

    t.printStackTrace() // TODO: handle errors more elegantly
    resp.setStatus(500)
    resp.getWriter.write(t.toString)
  }
}
