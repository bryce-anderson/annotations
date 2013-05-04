package jaxed.scalatramacros

import jaxed.servletmacros.ServletReqContext
import jaxed._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.scalatra.servlet.{RichResponse, RichRequest}


/**
 * @author Bryce Anderson
 *         Created on 5/4/13
 */
class ScalatraReqContext(method: RequestMethod,
                         routeParams: Params,
                         req: HttpServletRequest,
                         resp: HttpServletResponse)
  extends ServletReqContext(req.getRequestURI, method, routeParams, req, resp) { self =>

  //lazy val richRequest: RichRequest = RichRequest(req)
  //lazy val richResponse: RichResponse = RichResponse(resp)
  //def cookies = richRequest.cookies
  //
  //def cookie(key: String): Option[String] = cookies.get(key)

  override   def copy( path: String = self.path,  // Not used
                       method: RequestMethod = self.method,
                       routeParams: Params = self.routeParams,
                       req: HttpServletRequest = self.req,
                       resp: HttpServletResponse = self.resp) =
    new ScalatraReqContext(method, routeParams, req, resp)
}
