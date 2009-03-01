package com.twitter.service.cachet.test.mock

import cache.{Cache, CacheEntry}

class AlwaysHitCache(cacheEntry: CacheEntry) extends Cache {
  override def fetch(key: String) = Some(cacheEntry)

  override def put(key: String, value: CacheEntry) {}
}