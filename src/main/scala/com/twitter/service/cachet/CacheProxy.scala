package com.twitter.service.cachet

import java.lang.String
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import net.sf.ehcache._

class CacheProxy(cache: Ehcache, CacheEntry: ResponseWrapper => CacheEntry) {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) = {
    val element = cache.get(request.getQueryString)

    if (element == null) {
      fetch(request, response, chain)
    } else {
      val cacheEntry = element.getObjectValue.asInstanceOf[CacheEntry]
      if (cacheEntry.isTransparent) {
        cacheEntry.writeTo(response)
      } else {
        fetch(request, response, chain)
      }
    }
  }

  private def fetch(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    val responseWrapper = new ResponseWrapper(response)
    chain.doFilter(request, responseWrapper)
    val cacheEntry = CacheEntry(responseWrapper)
    if (cacheEntry.isCachable) {
      cache.put(new Element(request.getQueryString, cacheEntry))
    }
  }
}