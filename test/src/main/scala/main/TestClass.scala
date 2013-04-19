package main

import javax.ws.rs.GET

/**
 * @author brycea
 *         Created on 4/18/13 at 2:46 PM
 */

class TestClass {

  @GET
  def routeOne() = "route one"

  @GET
  def routeTwo(name: String) = s"The route received the name $name"

}
