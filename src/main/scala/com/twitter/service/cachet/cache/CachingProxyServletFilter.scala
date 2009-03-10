package com.twitter.service.cachet

import cache._
import javax.servlet._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import strategy.{Receive, Fetch}

class CachingProxyServletFilter extends Filter {
  var config = null: FilterConfig
  var receive = null: Receive

  def init(c: FilterConfig) {
    config = c
    val cache = new TransparentCache(Ehcache)
    val fetch = new Fetch(cache, ResponseCapturer, FreshResponseCacheEntry)
    receive = new Receive(cache, fetch)
  }

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    receive(request.asInstanceOf[HttpServletRequest], response.asInstanceOf[HttpServletResponse], chain)
  }

  def destroy {}
}