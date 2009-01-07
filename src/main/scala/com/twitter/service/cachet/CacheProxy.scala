package com.twitter.service.cachet

import java.lang.String
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import net.sf.ehcache._

class CacheProxy(cache: Ehcache) {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) = {
    val queryString: String = request.getQueryString()
    val element = cache.get(queryString)

    if (element == null) {
      val responseWrapper = new ResponseWrapper(response)
      chain.doFilter(request, responseWrapper)
      val cacheEntry: CacheEntry = new CacheEntry(responseWrapper)
      cache.put(new Element(queryString, cacheEntry))
      cacheEntry
    } else {
      element.getObjectValue.asInstanceOf[CacheEntry]
    }
  }
}