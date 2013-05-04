/**
 * @author Bryce Anderson
 *         Created on 5/4/13
 */
import org.scalatra._

import javax.servlet.ServletContext
import jaxed.scalatra.Main

class ScalatraBootstrap extends LifeCycle {

  println("Bootstrap hello")

  override def init(context: ServletContext) {
    context mount (new Main, "/")
//    context.mount(new CookiesExample, "/cookies-example")
//    context.mount(new BasicAuthExample, "/basic-auth")
//    context.mount(new FileUploadExample, "/upload")
//    context.mount(new FilterExample, "/")
//    context.mount(new AtmosphereChat, "/atmosphere")
//    context.mount(new TemplateExample, "/")

  }
}