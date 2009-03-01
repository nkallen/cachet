package com.twitter.service.cachet.servlet

import cache._
import javax.servlet._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import strategy.{Receive, Fetch}

class CacheProxyServletFilter extends Filter {
  var config = null: FilterConfig
  var get = null: Receive

  def init(c: FilterConfig) {
    config = c
    val cache = new TransparentCache(Ehcache)
    val fetch = new Fetch(cache, ResponseCapturer, FreshResponseCacheEntry)
    get = new Receive(cache, fetch)
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    get(request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse], chain)
  }

  def destroy {}
}