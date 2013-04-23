package jaxmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author Bryce Anderson
 *         Created on 4/22/13 at 12:52 PM
 */
trait RouteExceptionHandler {
  // This method should be stacked using super calls to created an exception pipeline.
  protected def handleException(t: Throwable, path: String, req: HttpServletRequest, resp: HttpServletResponse) {
    t.printStackTrace() // TODO: handle errors more elegantly
    resp.setStatus(500)
    resp.getWriter.write(t.toString)
    //throw t
  }
}
