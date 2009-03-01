package com.twitter.service.cachet.cache

class TransparentCache(cache: Cache) extends Cache {
  def fetch(key: String) =
    cache.fetch(key) filter (_.isTransparent)

  def put(key: String, value: CacheEntry) = {
    if (value.isCachable) cache.put(key, value)
  }
}