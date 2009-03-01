package com.twitter.service.cachet

import java.lang.String
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.servlet.FilterChain

class CacheProxy(cache: Cache, ResponseCapturer: HttpServletResponse => ResponseCapturer, CacheEntry: ResponseCapturer => CacheEntry) {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) = {
    val cacheEntry = cache.get(request.getQueryString)

    cacheEntry map {
      cacheEntry =>
              if (cacheEntry.isTransparent) {
                cacheEntry.writeTo(response)
              } else {
                fetch(request, response, chain)
              }
    } getOrElse fetch(request, response, chain)
  }

  private def fetch(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    val responseCapturer = ResponseCapturer(response)
    chain.doFilter(request, responseCapturer)
    val cacheEntry = CacheEntry(responseCapturer)
    if (cacheEntry.isCachable) {
      cache.put(request.getQueryString, cacheEntry)
    }
  }
}