package com.twitter.service.cachet.cache

import javax.servlet.http.HttpServletResponse

trait CacheEntry {
  def isTransparent: Boolean

  def isCachable: Boolean

  def writeTo(response: HttpServletResponse)
}