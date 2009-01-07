package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import net.sf.ehcache._

import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object CacheProxySpec extends Specification with JMocker with ClassMocker {
  "CacheProxy" should {
    var proxy: CacheProxy = null
    var cache: Ehcache = null
    var chain: FilterChain = null
    var request: FakeHttpServletRequest = null
    var response: HttpServletResponse = null
    var cacheEntry: CacheEntry = null

    doBefore {
      cache = mock(classOf[Ehcache])
      chain = mock(classOf[FilterChain])
      cacheEntry = mock(classOf[CacheEntry])
      proxy = new CacheProxy(cache, blah => cacheEntry)
      request = new FakeHttpServletRequest
      request.queryString = "/foo"

      response = new FakeHttpServletResponse
    }

    "apply" >> {
      def itFetches {
        expect { one(chain).doFilter(a[HttpServletRequest], a[ResponseWrapper]) }
      }
      def whenTheResourceIsCachable(b: Boolean) { expect { one(cacheEntry).isCachable willReturn(b) } }

      "when there is a cache miss" >> {
        def whenThereIsACacheMiss { expect { one(cache).get(request.queryString) willReturn (null: Element) } }

        "when the resource is cachable" >> {
          "invokes the filter, storing the result" >> {
            whenThereIsACacheMiss
            whenTheResourceIsCachable(true)
            itFetches
            expect { one(cache).put(a[Element]) }

            proxy(request, response, chain) mustEqual(cacheEntry)
          }
        }

        "when the resource is not cachable" >> {
          "invokes the filter, but does not store the result" >> {
            whenThereIsACacheMiss
            whenTheResourceIsCachable(false)
            itFetches
            expect { never(cache).put(a[Element]) }

            proxy(request, response, chain) mustEqual(cacheEntry)
          }
        }
      }

      "when there is a cache hit" >> {
        def whenThereIsACacheHit { expect { one(cache).get(request.queryString) willReturn (new Element(request.queryString, cacheEntry)) } }
        def whenTheCacheEntryIsTransparent(b: Boolean) { expect { one(cacheEntry).isTransparent willReturn(b) } }

        "when the cache entry is tranparent" >> {
          "returns the response from cache" >> {
            whenTheCacheEntryIsTransparent(true)
            whenThereIsACacheHit
            
            proxy(request, response, chain) mustEqual(cacheEntry)
          }
        }

        "when the cache entry is opaque" >> {
          "invokes the filter, storing the result" >> {
            whenTheResourceIsCachable(true)
            whenTheCacheEntryIsTransparent(false)
            whenThereIsACacheHit
            itFetches
            expect { one(cache).put(a[Element]) }

            proxy(request, response, chain) mustEqual(cacheEntry)
          }
        }
      }
    }
  }
}