package jaxed
package servletmacros

import javax.servlet.http.{Cookie, HttpServletResponse, HttpServletRequest}

/**
 * @author Bryce Anderson
 *         Created on 5/1/13
 */
class ServletReqContext(val path: String,
                             val method: RequestMethod,
                             val routeParams: Params,
                             val req: HttpServletRequest,
                             val resp: HttpServletResponse) extends RequestContext { self =>

  protected lazy val queryParams = {
    val str = req.getQueryString
    if (str != null) QueryParams.apply(str)
    else EmptyParams
  }

  private lazy val cookies: Map[String, String] = req.getCookies.map(c => (c.getName, c.getValue)).toMap

  def queryParam(name: String): Option[String] = queryParams.get(name)

  def routeParam(name: String): Option[String] = routeParams.get(name)

  def formParam(name: String): Option[String] = Option(req.getParameter(name))

  def getCookie(name: String): Option[String] = cookies.get(name)

  def setCookie(name: String, value: String): Unit = resp.addCookie(new Cookie(name, value))

  def subPath(newPath: String, newParams: Params) =
    self.copy(path = newPath, routeParams = if(newParams.isEmpty) routeParams else routeParams ++ newParams)

  def addParams(newParams: Params) =
    self.copy(routeParams = if(newParams.isEmpty) routeParams else routeParams ++ newParams)

  def getURI = req.getRequestURI

  def copy( path: String = self.path,
            method: RequestMethod = self.method,
            routeParams: Params = self.routeParams,
            req: HttpServletRequest = self.req,
            resp: HttpServletResponse = self.resp) =
    new ServletReqContext(path, method, routeParams, req, resp)
}
