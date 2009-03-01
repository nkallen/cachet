package com.twitter.service.cachet

import _root_.javax.servlet.FilterChain
import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}

class Fetch(cache: Cache,
           ResponseCapturer: HttpServletResponse => ResponseCapturer,
           CacheEntry: ResponseCapturer => CacheEntry) {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) = {
    val responseCapturer = ResponseCapturer(response)
    chain.doFilter(request, responseCapturer)
    val cacheEntry = CacheEntry(responseCapturer)
    cacheEntry.store(cache, request.getQueryString)
    cacheEntry
  }
}