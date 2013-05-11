package main

import jaxed.servlet.{RouteNode, AnnotationHandler}
import javax.ws.rs.GET


class Main extends AnnotationHandler {
  class RerouteSrc {
    @GET def main = {
      <html><body>
        <a href={route2.url(Map("money" -> "2.33432")).get}>Go to Dest</a>
      </body></html>
    }
  }

  class RerouteDest {
    @GET def go(money: Double) = s"Got rerouted! $money"
  }

  val route1 = mapClass[RerouteSrc]("/reroutesrc")
  val route2 = mapClass[RerouteDest]("/reroutedest/:money")

  mapClass[TestClass]("/foo/:bar/cats")
  mapClass[TestClass2]("/testclass2/:bar/cats")
  mapClass[TestClass3]("/testclass3/:bar")
  mapClass[FutureTest]("/future")
  mapClass[WithConstrutor]("/withConstructor")
  mapClass[GetCookieTest]("/getcookie")
  mapClass[SetCookieTest]("/setcookie")
  mapClass[WithFilter]("/withfilter")

  class OneTwoThree {
    @GET def get(one: String, two: String, three: String) = s"You found it! $one, $two, $three"
  }

  val route = newNode("/one/:one").newNode("/two/:two").mapClass[OneTwoThree]("/three/:three")
}
