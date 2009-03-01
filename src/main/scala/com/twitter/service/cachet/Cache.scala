package com.twitter.service.cachet

trait Cache {
  def get(key: String)(miss: => CacheEntry) = fetch(key) getOrElse miss

  def put(key: String, value: CacheEntry)

  protected def fetch(key: String): Option[CacheEntry]
}