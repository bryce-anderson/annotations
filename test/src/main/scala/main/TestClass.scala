package main

import javax.ws.rs.GET

import javax.ws.rs.FormParam
import javax.ws.rs.QueryParam

/**
 * @author Bryce Anderson
 *         Created on 4/18/13 at 2:46 PM
 */

class TestClass {

//  @GET
//  def routeOne() = "route one"
//
//  @POST
//  def routeTwo(bar: String, @FormParam("name") name: String) = s"The route received the name $name"

  @GET
  def  routeThree(bar: String) = s"The route received $bar"

}

class TestClass2 {
  @GET
  def routeOne(bar: String, @QueryParam("query") query: String) = s"routeOne: bar = $bar, query = $query"
}
