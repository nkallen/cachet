package com.twitter.service.cachet.cache

trait CacheEntry {
  def isTransparent: Boolean

  def isCachable: Boolean
}