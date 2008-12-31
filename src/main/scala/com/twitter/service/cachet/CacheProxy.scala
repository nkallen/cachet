package com.twitter.service.cachet

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain
import net.sf.ehcache._

class CacheProxy(cache: Ehcache) {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) = {
    val element = cache.get("asdf")
    if (element == null) {
      val responseWrapper = new ResponseWrapper(response)
      chain.doFilter(request, responseWrapper)
      cache.put(new Element("asdf", responseWrapper))
      responseWrapper
    } else {
      element.getObjectValue.asInstanceOf[CacheEntry].response
    }
  }
}