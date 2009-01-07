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
      "when there is a cache miss" >> {
        "when the resource is cachable" >> {
          "invokes the filter, storing the result" >> {
            expect { one(cache).get(request.queryString) willReturn (null: Element) }
            expect { one(cacheEntry).isCachable willReturn(true) }
            expect { one(chain).doFilter(a[HttpServletRequest], a[ResponseWrapper]) }
            expect { one(cache).put(a[Element]) }

            proxy(request, response, chain) mustEqual(cacheEntry)
          }
        }

        "when the resource is not cachable" >> {
          "invokes the filter, but does not store the result" >> {
            expect { one(cache).get(request.queryString) willReturn (null: Element) }            
            expect { one(cacheEntry).isCachable willReturn(false) }
            expect { one(chain).doFilter(a[HttpServletRequest], a[ResponseWrapper]) }
            expect { never(cache).put(a[Element]) }

            proxy(request, response, chain) mustEqual(cacheEntry)
          }
        }
      }

      "when there is a cache hit" >> {
        "when the cache entry is tranparent" >> {
          "returns the response from cache" >> {
            expect { one(cacheEntry).isTransparent willReturn(true) }
            val responseWrapper = new ResponseWrapper(response)
            expect { one(cache).get(request.queryString) willReturn (new Element(request.queryString, cacheEntry)) }
            proxy(request, response, chain) mustEqual(cacheEntry)
          }
        }

        "when the cache entry is opaque" >> {
          "invokes the filter, storing the result" >> {
            expect { one(cacheEntry).isTransparent willReturn(false) }
            expect { one(cacheEntry).isCachable willReturn(true) }
            val responseWrapper = new ResponseWrapper(response)
            expect { one(cache).get(request.queryString) willReturn (new Element(request.queryString, cacheEntry)) }
            expect { one(chain).doFilter(a[HttpServletRequest], a[ResponseWrapper]) }
            expect { one(cache).put(a[Element]) }

            proxy(request, response, chain) mustEqual(cacheEntry)
          }
        }
      }
    }
  }
}