package servletmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import jaxed.RequestContext

/**
 * @author Bryce Anderson
 *         Created on 4/22/13
 */
trait RouteExceptionHandler {
  // This method should be stacked using super calls to created an exception pipeline.
  protected def handleException(t: Throwable, path: RequestContext, req: HttpServletRequest, resp: HttpServletResponse) {
    t.printStackTrace() // TODO: handle errors more elegantly
    resp.setStatus(500)
    resp.getWriter.write(t.toString)
  }
}
