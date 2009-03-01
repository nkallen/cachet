package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import net.sf.ehcache.Ehcache
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object CacheProxySpec extends Specification with JMocker with ClassMocker {
  "CacheProxy" should {
    var proxy: CacheProxy = null
    var cache: Cache = null
    var chain: FilterChain = null
    var request: FakeHttpServletRequest = null
    var response: HttpServletResponse = null
    var responseCapturer: ResponseCapturer = null
    var cacheEntry: CacheEntry = null

    doBefore{
      cache = mock[Cache]
      chain = mock[FilterChain]
      cacheEntry = mock[CacheEntry]
      request = new FakeHttpServletRequest
      request.queryString = "/foo"
      response = new FakeHttpServletResponse
      responseCapturer = new ResponseCapturer(response)

      proxy = new CacheProxy(cache, blar => responseCapturer, blah => cacheEntry)
    }

    "apply" >> {
      def itFetches {
        expect{one(chain).doFilter(request, responseCapturer)}
      }
      def whenTheResourceIsCachable(b: Boolean) {expect{allowing(cacheEntry).isCachable willReturn b}}

      "when there is a cache miss" >> {
        def whenThereIsACacheMiss {expect{allowing(cache).get(request.queryString) willReturn None}}

        "when the resource is cachable" >> {
          "invokes the filter, storing the result" >> {
            whenThereIsACacheMiss
            whenTheResourceIsCachable(true)
            itFetches
            expect{one(cache).put(request.queryString, cacheEntry)}

            proxy(request, response, chain)
          }
        }

        "when the resource is not cachable" >> {
          "invokes the filter, but does not store the result" >> {
            whenThereIsACacheMiss
            whenTheResourceIsCachable(false)
            itFetches
            expect{never(cache).put(request.queryString, cacheEntry)}

            proxy(request, response, chain)
          }
        }
      }

      "when there is a cache hit" >> {
        def whenThereIsACacheHit {expect{allowing(cache).get(request.queryString) willReturn Some(cacheEntry)}}
        def whenTheCacheEntryIsTransparent(b: Boolean) {expect{allowing(cacheEntry).isTransparent willReturn b}}

        "when the cache entry is tranparent" >> {
          "returns the response from cache" >> {
            whenTheCacheEntryIsTransparent(true)
            whenThereIsACacheHit

            expect{one(cacheEntry).writeTo(response)}
            proxy(request, response, chain)
          }
        }

        "when the cache entry is opaque" >> {
          "invokes the filter, storing the result" >> {
            whenTheResourceIsCachable(true)
            whenTheCacheEntryIsTransparent(false)
            whenThereIsACacheHit
            itFetches
            expect{one(cache).put(request.queryString, cacheEntry)}

            proxy(request, response, chain)
          }
        }
      }
    }
  }
}