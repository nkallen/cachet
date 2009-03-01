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
    //    var responseCapturer: ResponseCapturer = null
    var cacheEntry: CacheEntry = null

    doBefore{
      cache = mock[Cache]
      chain = mock[FilterChain]
      cacheEntry = mock[CacheEntry]
      request = mock[HttpServletRequest]
      response = mock[HttpServletResponse]
      //      responseCapturer = new ResponseCapturer(response)

      get = new Get(cache, (request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) => cacheEntry)
    }

    "apply" >> {
      //      def itFetches {
      //        expect{one(chain).doFilter(request, responseCapturer)}
      //      }
      //      def whenTheResourceIsCachable(b: Boolean) {expect{allowing(cacheEntry).isCachable willReturn b}}

      //      "when there is a cache miss" >> {
      //        def whenThereIsACacheMiss {expect{allowing(cache).get(request.queryString) willReturn None}}
      //
      //        "when the resource is cachable" >> {
      //          "invokes the filter, storing the result" >> {
      //            whenThereIsACacheMiss
      //            whenTheResourceIsCachable(true)
      //            itFetches
      //            expect{one(cache).put(request.queryString, cacheEntry)}
      //
      //            proxy(request, response, chain)
      //          }
      //        }
      //
      //        "when the resource is not cachable" >> {
      //          "invokes the filter, but does not store the result" >> {
      //            whenThereIsACacheMiss
      //            whenTheResourceIsCachable(false)
      //            itFetches
      //            expect{never(cache).put(request.queryString, cacheEntry)}
      //
      //            proxy(request, response, chain)
      //          }
      //        }
      //      }

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