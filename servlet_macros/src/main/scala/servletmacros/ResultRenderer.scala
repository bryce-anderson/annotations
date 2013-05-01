package servletmacros

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.{ExecutionContext, Future}

import ExecutionContext.Implicits.global

/**
 * @author Bryce Anderson
 *         Created on 4/23/13
 */

/*
Trait that takes responsibility for rendering the result of a route. To add new formats, stack
more traits and use a super call to pass results down the chain.
 */
trait ResultRenderer {
  def renderResponse(req: HttpServletRequest, resp: HttpServletResponse, result: Any): Unit  = result match {
    case _: scala.runtime.BoxedUnit => // Nothing
    case s: String => resp.getWriter().write(s)
    case d: Array[Byte] => resp.getOutputStream().write(d)
    case xml: xml.NodeSeq =>
      resp.setContentType("text/html")
      resp.getWriter().write(xml.toString())
    case Some(r) => renderResponse(req, resp, r)
    case f: Future[Any] =>
      val async = req.startAsync()
      f.onSuccess { case result =>
        renderResponse(req, resp, result)
        async.complete()
      }
    case e => throw new java.lang.UnsupportedOperationException(s"Type '${e.getClass}' not supported by the pipeline. " +
                      "Try mixing a ResultRenderer trait into the route node.")

  }
}
