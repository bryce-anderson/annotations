package main

import javax.ws.rs._


/**
 * @author Bryce Anderson
 *         Created on 4/18/13 at 2:46 PM
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
