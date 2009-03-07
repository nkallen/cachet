package com.twitter.service.cachet.cache

import net.sf.ehcache

object Ehcache extends Cache {
  val cacheManager = ehcache.CacheManager.getInstance
  val cache = new ehcache.Cache("Cache", 5000, false, true, 5000, 5000)
  cacheManager.addCache(cache)

  def fetch(key: String) = {
    val element = cache.get(key)
    if (element != null) {
      Some(element.getObjectValue.asInstanceOf[CacheEntry])
    } else {
      None
    }
  }

  def put(key: String, value: CacheEntry) {
    cache.put(new ehcache.Element(key, value))
  }
}
