package jaxed.servlet

import javax.servlet.http._
import javax.servlet._
import java.util
import java.util.{EventListener, Locale}
import java.io.{InputStream, BufferedReader}
import java.security.Principal
import javax.servlet.descriptor.JspConfigDescriptor
import javax.servlet.FilterRegistration.Dynamic
import java.net.URL

/**
 * @author Bryce Anderson
 *         Created on 5/19/13
 */

/**
 * Trait does nothing but provide default implementations that will throw exceptions. Override the methods you need.
 */
trait MockServletRequest extends HttpServletRequest {
  def getAuthType: String = ???

  def getCookies: Array[Cookie] = ???

  def getDateHeader(name: String): Long = ???

  def getHeader(name: String): String = ???

  def getHeaders(name: String): util.Enumeration[String] = ???

  def getHeaderNames: util.Enumeration[String] = ???

  def getIntHeader(name: String): Int = ???

  def getMethod: String = ???

  def getPathInfo: String = ???

  def getPathTranslated: String = ???

  def getContextPath: String = ???

  def getQueryString: String = ???

  def getRemoteUser: String = ???

  def isUserInRole(role: String): Boolean = ???

  def getUserPrincipal: Principal = ???

  def getRequestedSessionId: String = ???

  def getRequestURI: String = ???

  def getRequestURL: StringBuffer = ???

  def getServletPath: String = ???

  def getSession(create: Boolean): HttpSession = ???

  def getSession: HttpSession = ???

  def isRequestedSessionIdValid: Boolean = ???

  def isRequestedSessionIdFromCookie: Boolean = ???

  def isRequestedSessionIdFromURL: Boolean = ???

  def isRequestedSessionIdFromUrl: Boolean = ???

  def authenticate(response: HttpServletResponse): Boolean = ???

  def login(username: String, password: String) {}

  def logout() {}

  def getParts: util.Collection[Part] = ???

  def getPart(name: String): Part = ???

  def getAttribute(name: String): AnyRef = ???

  def getAttributeNames: util.Enumeration[String] = ???

  def getCharacterEncoding: String = ???

  def setCharacterEncoding(env: String) {}

  def getContentLength: Int = ???

  def getContentType: String = ???

  def getInputStream: ServletInputStream = ???

  def getParameter(name: String): String = ???

  def getParameterNames: util.Enumeration[String] = ???

  def getParameterValues(name: String): Array[String] = ???

  def getParameterMap: util.Map[String, Array[String]] = ???

  def getProtocol: String = ???

  def getScheme: String = ???

  def getServerName: String = ???

  def getServerPort: Int = ???

  def getReader: BufferedReader = ???

  def getRemoteAddr: String = ???

  def getRemoteHost: String = ???

  def setAttribute(name: String, o: Any) {}

  def removeAttribute(name: String) {}

  def getLocale: Locale = ???

  def getLocales: util.Enumeration[Locale] = ???

  def isSecure: Boolean = ???

  def getRequestDispatcher(path: String): RequestDispatcher = ???

  def getRealPath(path: String): String = ???

  def getRemotePort: Int = ???

  def getLocalName: String = ???

  def getLocalAddr: String = ???

  def getLocalPort: Int = ???

  lazy val getServletContext: ServletContext = new ServletContext {

    def createFilter[T <: javax.servlet.Filter](x$1: Class[T]): T = ???
    def declareRoles(x$1: String*): Unit = ???

    def addFilter(x$1: String,x$2: Class[_ <: javax.servlet.Filter]): javax.servlet.FilterRegistration.Dynamic = ???

    def addFilter(x$1: String,x$2: javax.servlet.Filter): javax.servlet.FilterRegistration.Dynamic = ???

    def getContextPath: String = ???

    def getContext(uripath: String): ServletContext = ???

    def getMajorVersion: Int = ???

    def getMinorVersion: Int = ???

    def getEffectiveMajorVersion: Int = ???

    def getEffectiveMinorVersion: Int = ???

    def getMimeType(file: String): String = ???

    def getResourcePaths(path: String): util.Set[String] = ???

    def getResource(path: String): URL = ???

    def getResourceAsStream(path: String): InputStream = ???

    def getRequestDispatcher(path: String): RequestDispatcher = ???

    def getNamedDispatcher(name: String): RequestDispatcher = ???

    def getServlet(name: String): Servlet = ???

    def getServlets: util.Enumeration[Servlet] = ???

    def getServletNames: util.Enumeration[String] = ???

    def log(msg: String) {}

    def log(exception: Exception, msg: String) {}

    def log(message: String, throwable: Throwable) {}

    def getRealPath(path: String): String = ???

    def getServerInfo: String = ???

    def getInitParameter(name: String): String = ???

    def getInitParameterNames: util.Enumeration[String] = ???

    def setInitParameter(name: String, value: String): Boolean = ???

    def getAttribute(name: String): AnyRef = ???

    def getAttributeNames: util.Enumeration[String] = ???

    def setAttribute(name: String, `object`: Any) {}

    def removeAttribute(name: String) {}

    def getServletContextName: String = ???

    def addServlet(servletName: String, className: String): ServletRegistration.Dynamic = ???

    def addServlet(servletName: String, servlet: Servlet): ServletRegistration.Dynamic = ???

    def addServlet(servletName: String, servletClass: Class[_ <: Servlet]): ServletRegistration.Dynamic = ???

    def createServlet[T <: Servlet](clazz: Class[T]): T = ???

    def getServletRegistration(servletName: String): ServletRegistration = ???

    def getServletRegistrations: util.Map[String, _ <: ServletRegistration] = ???

    def addFilter(filterName: String, className: String): Dynamic = ???

    def getFilterRegistration(filterName: String): FilterRegistration = ???

    def getFilterRegistrations: util.Map[String, _ <: FilterRegistration] = ???

    def getSessionCookieConfig: SessionCookieConfig = ???

    def setSessionTrackingModes(sessionTrackingModes: util.Set[SessionTrackingMode]) {}

    def getDefaultSessionTrackingModes: util.Set[SessionTrackingMode] = ???

    def getEffectiveSessionTrackingModes: util.Set[SessionTrackingMode] = ???

    def addListener(className: String) {}

    def addListener[T <: EventListener](t: T) {}

    def addListener(listenerClass: Class[_ <: EventListener]) {}

    def createListener[T <: EventListener](clazz: Class[T]): T = ???

    def getJspConfigDescriptor: JspConfigDescriptor = ???

    def getClassLoader: ClassLoader = ???

  }

  def startAsync(): AsyncContext = ???

  def startAsync(servletRequest: ServletRequest, servletResponse: ServletResponse): AsyncContext = ???

  def isAsyncStarted: Boolean = ???

  def isAsyncSupported: Boolean = ???

  def getAsyncContext: AsyncContext = ???

  def getDispatcherType: DispatcherType = ???
}
