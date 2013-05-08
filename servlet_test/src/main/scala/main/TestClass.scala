package main

import javax.ws.rs._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.concurrent.Future
import jaxed.servlet.{ServletReqContext, Filter}


/**
 * @author Bryce Anderson
 *         Created on 4/18/13
 */

class TestClass {

  @POST
  def routeTwo(bar: String, @FormParam("name") name: String) =
    s"The route received the name $name"

  @GET
  def routeThree(bar: String) =
    s"The route received $bar"

}

class TestClass2 {
  @GET
  def routeGet(bar: Int)(@QueryParam("query") @DefaultValue("1") query: Int = -1) =
    <h2>routeGet: bar = {bar}, query = {query}</h2>

  @POST
  def routePost(bar: Int, @FormParam("form") form: Double = 3.2) =
    s"routePost: bar = $bar, form = $form"
}

class TestClass3 {
  @GET
  def routeGet(bar: Int, resp: HttpServletResponse) {
    resp.getWriter.write(s"TestClass3: bar = $bar, written directly.")
  }
}

class DoubleClass extends Filter {
  @GET
  def routeGet() = 3.14
}

class WithConstrutor(bar: String = "Default") {
  @GET
  def routeGet() = bar
}

class FutureTest {
  import scala.concurrent.ExecutionContext.Implicits.global
  @GET
  def routeGet() = Future("Hello future")
}

class GetCookieTest {
  @GET
  def cookieRoute(@CookieParam("cookie") cookie: String = "None") = s"Got cookie '$cookie'"
}

class SetCookieTest {
  import javax.servlet.http.Cookie
  @GET def cookieRoute(req: HttpServletResponse) = {
    req.addCookie(new Cookie("cookie", "Its a cookie!"))
    "Setting a cookie for you"
  }
}

trait MyFilter extends Filter {
  override def beforeFilter(context: ServletReqContext): Option[Any] = {
    context.resp.getWriter.write("Before Filter!\n")
    None
  }

  override def afterFilter(context: ServletReqContext, result: Option[Any]) = result.map(_ + "\nAfter Route!\n")
}

class WithFilter extends MyFilter {
  @GET
  def getReq() = "Hello world, with filters."
}
