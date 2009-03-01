package com.twitter.service.cachet.cache

import net.sf.ehcache._

object Ehcache extends Cache {
  val cache = CacheManager.getInstance.getEhcache("Name")

  def fetch(key: String) = {
    val element = cache.get(key)
    if (element != null) {
      Some(element.getObjectValue.asInstanceOf[CacheEntry])
    } else {
      None
    }
  }

  def put(key: String, value: CacheEntry) {
    cache.put(new Element(key, value))
  }
}
