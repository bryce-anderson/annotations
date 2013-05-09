package jaxed.servlet

/**
 * @author Bryce Anderson
 *         Created on 5/8/13
 */
trait DefaultResponses {
  def on404NotFound(context: ServletReqContext): Any = {
    context.resp.setStatus(404)
    <html><body>
      <h2>404 Not Found</h2><br/>
      {context.req.getRequestURL.toString}
    </body></html>
  }

}
