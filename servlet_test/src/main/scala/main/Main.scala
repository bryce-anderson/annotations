package main

import jaxed.servlet.{RouteBranch, AnnotationHandler}
import javax.ws.rs.GET
import jaxed.Get

object MainBranch extends RouteBranch("/branch") {
  addLeafRoute(Get, ""){ _ => "I'm a mounted BranchNode"}
}

class Main extends AnnotationHandler {
  mountBranch(MainBranch)

  class RerouteSrc {
    @GET def main = {
      <html><body>
        <a href={route2.url(Map("money" -> "2.33432")).get}>Go to Dest</a><br/>
        Got {route2.pathParamNames.toString()}
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
