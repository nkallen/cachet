package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._
import strategy.Fetch

object FetchSpec extends Specification with JMocker with ClassMocker {
  "Fetch" should {
    var fetch = null: Fetch
    var cache = null: Cache
    var chain = null: FilterChain
    var request = null: HttpServletRequest
    var response = null: HttpServletResponse
    var responseCapturer = null: ResponseCapturer
    var cacheEntry = null: CacheEntry

    doBefore{
      cache = mock[Cache]
      chain = mock[FilterChain]
      cacheEntry = mock[CacheEntry]
      request = mock[HttpServletRequest]
      response = mock[HttpServletResponse]
      responseCapturer = mock[ResponseCapturer]

      fetch = new Fetch(cache, response => responseCapturer, responseCapturer => cacheEntry)
    }

    "apply" >> {
      "invokes the chain and stores the cache entry" >> {
        expect{
          allowing(request).getQueryString willReturn "foo"
          one(chain).doFilter(request, responseCapturer)
          one(cacheEntry).store(cache, "foo")
        }
        fetch(request, response, chain)
      }
    }
  }
}