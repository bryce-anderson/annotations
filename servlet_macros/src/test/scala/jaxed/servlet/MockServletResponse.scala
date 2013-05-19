package jaxed.servlet

import javax.servlet.http.{Cookie, HttpServletResponse}
import java.util.Locale
import java.io.PrintWriter
import javax.servlet.ServletOutputStream
import java.util

/**
 * @author Bryce Anderson
 *         Created on 5/19/13
 */
trait MockServletResponse extends HttpServletResponse {
  def addCookie(cookie: Cookie) {}

  def containsHeader(name: String): Boolean = ???

  def encodeURL(url: String): String = ???

  def encodeRedirectURL(url: String): String = ???

  def encodeUrl(url: String): String = ???

  def encodeRedirectUrl(url: String): String = ???

  def sendError(sc: Int, msg: String) {}

  def sendError(sc: Int) {}

  def sendRedirect(location: String) {}

  def setDateHeader(name: String, date: Long) {}

  def addDateHeader(name: String, date: Long) {}

  def setHeader(name: String, value: String) {}

  def addHeader(name: String, value: String) {}

  def setIntHeader(name: String, value: Int) {}

  def addIntHeader(name: String, value: Int) {}

  def setStatus(sc: Int) {}

  def setStatus(sc: Int, sm: String) {}

  def getStatus: Int = ???

  def getHeader(name: String): String = ???

  def getHeaders(name: String): util.Collection[String] = ???

  def getHeaderNames: util.Collection[String] = ???

  def getCharacterEncoding: String = ???

  def getContentType: String = ???

  def getOutputStream: ServletOutputStream = ???

  def getWriter: PrintWriter = ???

  def setCharacterEncoding(charset: String) {}

  def setContentLength(len: Int) {}

  def setContentType(`type`: String) {}

  def setBufferSize(size: Int) {}

  def getBufferSize: Int = ???

  def flushBuffer() {}

  def resetBuffer() {}

  def isCommitted: Boolean = ???

  def reset() {}

  def setLocale(loc: Locale) {}

  def getLocale: Locale = ???
}
