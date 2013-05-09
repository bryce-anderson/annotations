package main

import javax.servlet._
import javax.servlet.http.HttpServletRequest

/**
 * @author Bryce Anderson
 *         Created on 5/9/13
 */
class StaticServletFilter extends Filter {
  val staticPath = "/static"
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, resp: ServletResponse, chain: FilterChain) {
    val req = request.asInstanceOf[HttpServletRequest]
    val reqPath = req.getRequestURI().substring(req.getContextPath().length())

    if (reqPath.startsWith(staticPath)) {
      chain.doFilter(req, resp)
    } else {
      req.getRequestDispatcher("/app" + req.getRequestURI).forward(req, resp)
    }

  }

  def destroy() {}
}
