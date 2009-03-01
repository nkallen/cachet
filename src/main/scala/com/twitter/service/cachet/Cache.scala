package com.twitter.service.cachet

trait Cache {
  def get(key: String): Option[CacheEntry]

  def put(key: String, value: CacheEntry)
}