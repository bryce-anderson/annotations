package main

import jaxmacros.{RouteNode}
import servletmacros.AnnotationHandler


class Main extends AnnotationHandler {
  val rootNode = RouteNode()
    .mapClass[TestClass]("/foo/:bar/cats")
    .mapClass[TestClass2]("/testclass2/:bar/cats")
    .mapClass[TestClass3]("/testclass3/:bar")
    .mapClass[FutureTest]("/future")
    .mapClass[WithConstrutor]("")
}
