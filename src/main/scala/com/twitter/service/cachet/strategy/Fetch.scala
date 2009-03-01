package com.twitter.service.cachet.strategy

import _root_.javax.servlet.FilterChain
import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}

class Fetch(cache: Cache,
           ResponseCapturer: HttpServletResponse => ResponseCapturer,
           CacheEntry: ResponseCapturer => CacheEntry) extends Function3[HttpServletRequest, HttpServletResponse, FilterChain, CacheEntry] {
  def apply(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): CacheEntry = {
    val responseCapturer = ResponseCapturer(response)
    chain.doFilter(request, responseCapturer)
    val cacheEntry = CacheEntry(responseCapturer)
    cacheEntry.store(cache, request.getQueryString)
    cacheEntry
  }
}