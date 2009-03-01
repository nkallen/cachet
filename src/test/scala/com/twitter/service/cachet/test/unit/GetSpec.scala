package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object GetSpec extends Specification with JMocker with ClassMocker {
  "Get" should {
    var get: Get = null
    var cache: Cache = null
    var chain: FilterChain = null
    var request: HttpServletRequest = null
    var response: HttpServletResponse = null
    var cacheEntry: CacheEntry = null

    doBefore{
      cache = mock[Cache]
      chain = mock[FilterChain]
      cacheEntry = mock[CacheEntry]
      request = mock[HttpServletRequest]
      response = mock[HttpServletResponse]

      get = new Get(cache, (request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) => cacheEntry)
    }

    "apply" >> {
      "when there is a cache hit" >> {
        "returns the response from cache" >> {
          expect{
            allowing(request).getQueryString willReturn "foo"
            allowing(cache).get("foo"){cacheEntry} willReturn cacheEntry
          }

          //          expect{one(cacheEntry).writeTo(response)}
          //          get(request, response, chain)
        }
      }
    }
  }
}