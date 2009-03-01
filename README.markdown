# Cachet

Cachet is an HTTP Reverse Cache Proxy written in Scala. It is comparable to Varnish or Rack::Cache. It implements the RFC2616 specification (at this time, only the Expiration model, but the Validation model is forthcoming) obeying Cache-Control headers and the like. It is very configurable so that you can use custom databases (Ehcache and Memcached are supported by default), and custom expiry strategies, and so on. It is designed so that you can layer on things like authentication, authorization, rate limiting, and arbitrary service composition strategies (e.g., Edge-Side Includes)--which is is, I think, something novel in an open-source reverse proxy.

## Choice of Language

Scala is a fast language, though not as fast as C. Unfortunately, cache proxies written in C are very inflexible. Consider Varnish. If you want cache rules that go beyond the RFC2616, you're basically screwed. The VCL extension language that comes with Varnish is too limited to do anything non-trivial. Applying authentication, authorization, or rate limiting in front of the proxy is basically impossible. If you need more flexible data-stores for cache partitioning, for example, you're screwed twice over. You can fork Varnish, and you will suffer the slow pace of development of writing C code. Varnish is also remarkably brittle and difficult to configure.

Rack::Cache is well designed but it is written in Ruby. This makes it extremely extensible--and maybe even a pleasure to work with--but about an order of magnitude slower than something written in Scala.

Scala is nearly as flexible and fun a language as Ruby. It offers greater guarantees from the static typing system and incredible performance. The Scala toolchain (test and mocking infrastructure and such) is immature, but the wealth of Java libraries is immense. On balance I think Scala is a better choice than Ruby or C for writing a Reverse proxy.

## Design Philosophy

Varnish's VCL configuration language is well designed, despite being inflexible. Rack::Cache takes a similar approach, creating a Ruby DSL that resembles VCL. I've preferred here to use more traditional OO and Functional programming techniques for achieving modularity. Basically, a lot of functions, a lot of dependency injection, the strategy pattern, composition over inheritance, etc. The system is loosely coupled in the traditional OO, Law of Demeter, tell-don't-ask sense. I know it is more configurable than VCL. I think it is also more configurable than Rack::Cache.