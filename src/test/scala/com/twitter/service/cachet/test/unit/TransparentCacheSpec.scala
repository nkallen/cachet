package com.twitter.service.cachet.test.unit

import cache.{Cache, TransparentCache, CacheEntry}
import com.twitter.service.cachet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.mortbay.jetty.client.{HttpClient, HttpExchange}
import org.specs._
import org.specs.mock._
import org.specs.mock.JMocker._
import com.twitter.service.cachet.test.mock._

object TransparentCacheSpec extends Specification with JMocker with ClassMocker {
  "TransparentCache" should {
    var hitCacheEntry = null: CacheEntry
    var missCacheEntry = null: CacheEntry
    var cache = null: Cache
    var transparentCache = null: TransparentCache

    doBefore{
      hitCacheEntry = mock[CacheEntry]
      missCacheEntry = mock[CacheEntry]
      cache = mock[Cache]
      transparentCache = new TransparentCache(cache)
    }

    "get(k)(miss)" >> {
      "when there is a cache miss" >> {
        "it invokes miss" >> {
          expect{allowing(cache).fetch("foo") willReturn None}
          var yielded = false
          transparentCache.get("foo"){yielded = true; hitCacheEntry} mustEqual hitCacheEntry
          yielded must beTrue
        }
      }

      "when there is a cache hit" >> {
        "when the entry is transparent" >> {
          "it returns the cacheEntry" >> {
            expect{allowing(cache).fetch("foo") willReturn Some(hitCacheEntry)}
            var yielded = false
            expect{allowing(hitCacheEntry).isTransparent willReturn true}
            transparentCache.get("foo"){yielded = true; missCacheEntry} mustEqual hitCacheEntry
            yielded must beFalse
          }
        }

        "when the entry is opaque" >> {
          "it invokes miss" >> {
            expect{allowing(cache).fetch("foo") willReturn Some(hitCacheEntry)}
            var yielded = false
            expect{allowing(hitCacheEntry).isTransparent willReturn false}
            transparentCache.get("foo"){yielded = true; missCacheEntry} mustEqual missCacheEntry
            yielded must beTrue
          }
        }
      }
    }

    "put(k,v)" >> {
      "when v.isCachable" >> {
        "it stores the object" >> {

        }
      }

      "when !v.isCachable" >> {
        "it does not store the object" >> {

        }
      }
    }
  }
}