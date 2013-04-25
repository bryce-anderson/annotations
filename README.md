# annotations — Scala macro based, JAX-RS inspired servlet generation. #

## Build & run ##
sbt revolver plugin required in sbt build, remove otherwise.
sbt
project test
re-start

* Note there is a problem with sbt and the macro generation when changing the classes which are mapped. This can be fixed by commenting out the binding code, eg mapClass[A]("path"), compiling, and uncommenting. It would probably work with touch as well.

## Contact ##

- bryce.anderson22ATgmail.com
