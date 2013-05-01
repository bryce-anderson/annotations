package servletmacros

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author Bryce Anderson
 *         Created on 4/30/13
 */
class ServletBinding {

  val reqName = "req"
  val reqExpr = c.Expr[HttpServletRequest](Ident(newTermName(reqName)))
  val respName = "resp"
  val respExpr = c.Expr[HttpServletResponse](Ident(newTermName(respName)))

}
