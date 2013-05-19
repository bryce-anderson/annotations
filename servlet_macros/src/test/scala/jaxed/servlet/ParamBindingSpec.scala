package jaxed.servlet

import org.specs2.mutable.Specification
import jaxed.Get
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.ws.rs.GET
import javax.servlet.ServletContext

/**
 * @author Bryce Anderson
 *         Created on 5/19/13
 */
class ParamBindingSpec extends Specification {

  def ctx(path: String,
          params: Map[String, String] = Map.empty,
          req: HttpServletRequest = null,
          resp: HttpServletResponse = null) = new ServletReqContext(path, Get, params, req, resp)

  "Servlet Binding" should {
    "Bind primatives" in {
      class Test{
        @GET
        def test(in1: Int, in2: Long, in3: Double, in4: Float, in5: String) = s"$in1 $in2 $in3 $in4 $in5"
      }

      object Node extends MockNode("") {
        mapClass[Test]("")
      }

      Node.handle(ctx("",
        Map("in1" -> "1", "in2" -> "2", "in3" -> "3.14", "in4" -> "3.15", "in5" -> "string"))
      ) must_== Some("1 2 3.14 3.15 string")

    }

    "Bind Servlet Types" in {
      class Test {
        @GET
        def test(in1: HttpServletRequest, in2: HttpServletResponse, in3: ServletContext) = s"Success: $in1 $in2 $in3"
      }
      object Node extends MockNode("") {
        mapClass[Test]("")
      }
      val req = new MockServletRequest {}
      val resp = new MockServletResponse {}
      Node.handle(ctx("",
        req = req,
        resp = resp)) must_== Some(s"Success: $req $resp ${req.getServletContext}")


    }
  }

}
