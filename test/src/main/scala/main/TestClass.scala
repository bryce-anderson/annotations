package main

import javax.ws.rs._
import javax.servlet.http.HttpServletResponse


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
  def routeGet(bar: Int, @QueryParam("query") @DefaultValue("1") query: Int = -1) =
    s"routeGet: bar = $bar, query = $query"

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

class DoubleClass {
  @GET
  def routeGet() = 3.14
}

class WithConstrutor(bar: String = "Default") {
  @GET
  def routeGet() = bar
}
