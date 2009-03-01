package com.twitter.service.cachet

trait Cache {
  def get(key: String)(miss: => FreshResponseCacheEntry) = fetch(key) getOrElse miss

  def put(key: String, value: FreshResponseCacheEntry)

  protected def fetch(key: String): Option[FreshResponseCacheEntry]
}