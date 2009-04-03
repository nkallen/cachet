package com.twitter.service.cachet.cache.strategy

import javax.servlet.FilterChain
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import cache.{Cache, CacheEntry}

class Fetch(cache: Cache,
            ResponseCapturer: (HttpServletResponse, HttpServletResponse => Unit) => ResponseCapturer,
            CacheEntry: ResponseCapturer => CacheEntry) extends Function3[HttpServletRequest, HttpServletResponse, FilterChain, CacheEntry] {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): CacheEntry = {
    val responseCapturer = ResponseCapturer(response, chain.doFilter(request, _))
    val cacheEntry = CacheEntry(responseCapturer)
    cache.put(request.getQueryString, cacheEntry)
    cacheEntry
  }
}
