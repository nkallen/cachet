package com.twitter.service.cachet.test.mock

import cache.{Cache, CacheEntry}

class AlwaysMissCache extends Cache {
  override def fetch(key: String) = None

  override def put(key: String, value: CacheEntry) {}
}