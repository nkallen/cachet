package com.twitter.service.cachet.cache

import javax.servlet.http.HttpServletResponse

trait CacheEntry {
  /**
   * 2616 Section FIXME: find the correct section
   */
  def isTransparent: Boolean

  /**
   * 2616 Section FIXME: find the correct section
   */
  def isCachable: Boolean

  def writeTo(response: HttpServletResponse)
}
