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
    var hitCacheEntry = null: CacheEntry
    var missCacheEntry = null: CacheEntry

    doBefore{
      chain = mock[FilterChain]
      hitCacheEntry = mock[CacheEntry]
      missCacheEntry = mock[CacheEntry]
      request = mock[HttpServletRequest]
      response = mock[HttpServletResponse]
    }

    "apply" >> {
      "when there is a cache hit" >> {
        "invokes hitCacheEntry.writeTo(response)" >> {
          expect{allowing(request).getQueryString}
          cache = new AlwaysHitCache(hitCacheEntry)
          receive = new Receive(cache, (request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) => missCacheEntry)

          expect{one(hitCacheEntry).writeTo(response)}
          receive(request, response, chain)
        }
      }

      "when there is a cache miss" >> {
        "invokes missCacheEntry.writeTo(response)" >> {
          expect{allowing(request).getQueryString}
          cache = new AlwaysMissCache
          receive = new Receive(cache, (request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) => missCacheEntry)

          expect{one(missCacheEntry).writeTo(response)}
          receive(request, response, chain)
        }
      }
    }
  }
}