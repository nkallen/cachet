package com.twitter.service.cachet.test.unit

import cache.{Cache, CacheEntry}
import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._
import strategy.Receive

object ReceiveSpec extends Specification with JMocker with ClassMocker {
  "Receive" should {
    var receive = null: Receive
    var cache = null: Cache
    var chain = null: FilterChain
    var request = null: HttpServletRequest
    var response = null: HttpServletResponse
    var cacheEntry = null: CacheEntry

    doBefore{
      cache = mock[Cache]
      chain = mock[FilterChain]
      cacheEntry = mock[CacheEntry]
      request = mock[HttpServletRequest]
      response = mock[HttpServletResponse]

      receive = new Receive(cache, (request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) => cacheEntry)
    }

    "apply" >> {
      "when there is a cache hit" >> {
        "returns the response from cache" >> {
          expect{
            allowing(request).getQueryString willReturn "foo"
            allowing(cache).get("foo"){cacheEntry} willReturn cacheEntry
          }

          //          expect{one(cacheEntry).writeTo(response)}
          //          receive(request, response, chain)
        }
      }
    }
  }
}