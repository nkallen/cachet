package com.twitter.service.cachet.cache

import _root_.javax.servlet.http.HttpServletResponse

trait CacheEntry {
  def isTransparent: Boolean

  def isCachable: Boolean

  def writeTo(response: HttpServletResponse)

  def store(cache: Cache, key: String) {
    if (isCachable) {
      cache.put(key, this)
    }
  }
}