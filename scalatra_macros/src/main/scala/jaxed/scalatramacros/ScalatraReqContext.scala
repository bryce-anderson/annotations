package jaxed.scalatramacros

import jaxed._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.scalatra.servlet.{RichResponse, RichRequest}
import org.scalatra.SweetCookies
import org.scalatra.CookieSupport.SweetCookiesKey
import org.scalatra.servlet.RichResponse


/**
 * @author Bryce Anderson
 *         Created on 5/4/13
 */
class ScalatraReqContext(method: RequestMethod,
                         routeParams: Params,
                         req: HttpServletRequest,
                         resp: HttpServletResponse)
  extends ServletReqContext(null, method, routeParams, req, resp) { self =>

  lazy val richRequest: RichRequest = RichRequest(req)
  lazy val richResponse: RichResponse = RichResponse(resp)
  private def cookies = richRequest.get(SweetCookiesKey).orNull.asInstanceOf[SweetCookies]

  override def setCookie(name: String, value: String) { cookies += ((name, value)) }
  override def getCookie(name: String): Option[String] = cookies.get(name)

  def cookie(key: String): Option[String] = cookies.get(key)

  override   def copy( path: String = null,  // Not used
                       method: RequestMethod = self.method,
                       routeParams: Params = self.routeParams,
                       req: HttpServletRequest = self.req,
                       resp: HttpServletResponse = self.resp) =
    new ScalatraReqContext(method, routeParams, req, resp)
}
