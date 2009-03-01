package com.twitter.service.cachet.cache

trait Cache {
  def fetch(key: String): Option[CacheEntry]

  def put(key: String, value: CacheEntry)

  def get(key: String)(miss: => CacheEntry) = {
    fetch(key) getOrElse miss
  }
}