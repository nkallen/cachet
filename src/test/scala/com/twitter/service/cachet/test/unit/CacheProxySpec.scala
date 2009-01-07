package com.twitter.service.cachet.test.unit

import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import net.sf.ehcache._

import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object CacheProxySpec extends Specification with JMocker {
  "CacheProxy" should {
    var proxy: CacheProxy = null
    var cache: Ehcache = null
    var chain: FilterChain = null
    var request: FakeHttpServletRequest = null
    var response: HttpServletResponse = null

    doBefore {
      cache = mock(classOf[Ehcache])
      chain = mock(classOf[FilterChain])
      proxy = new CacheProxy(cache)
      request = new FakeHttpServletRequest
      request.queryString = "/foo"

      response = new FakeHttpServletResponse
    }

    "proxy" >> {
      "when there is a cache miss" >> {
        "invokes the filter, storing the result" >> {
          expect { one(cache).get(request.queryString) willReturn(null: Element) }
          expect { one(chain).doFilter(a[HttpServletRequest], a[ResponseWrapper]) }
          expect { one(cache).put(a[Element]) }

          proxy(request, response, chain) must haveClass(classOf[CacheEntry])
        }
      }

      "when there is a cache hit" >> {
        "returns the response from cache" >> {
          val responseWrapper = new ResponseWrapper(response)
          val cacheEntry = new CacheEntry(responseWrapper)
          expect { one(cache).get(request.queryString) willReturn(new Element(request.queryString, cacheEntry)) }
          proxy(request, response, chain) must be_==(cacheEntry)
        }
      }
    }
  }
}