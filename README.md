# annotations — Scala macro based, JAX-RS inspired route generation. #

## Build & run ##

```sh
$ sbt
 project servlet_test
 container:start
```

* Use JAX-RS style annotations (in fact, use their annotations) to turn plain scala classes into routes
* All compile time for no reflection overhead
* Automatic type conversion from string parameters other primitive types with ability to add complex conversion
* Extensible- Macro implementations based on traits that can be overridden and extended

Works with Scalatra and has a default servlet routing system.

## Contact ##

- bryce.anderson22@gmail.com
